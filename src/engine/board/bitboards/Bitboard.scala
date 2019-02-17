package engine.board.bitboards

import engine.board.Piece._
import engine.board.bitboards.Bitboard.U64
import engine.movegen.Location._
import engine.movegen.Move.{BitboardMove, LocationMove}

import scala.annotation.tailrec
import engine.board._
import engine.movegen._
import engine.movegen.bitboards._
import engine.search.{AlphaBeta, AlphaBetaMax}

/**
  * Created by melvic on 8/5/18.
  */
object Bitboard {
  type U64 = Long
  type SetwiseOp = (U64, Int) => U64

  /**
    * The side-to-move bitboards come first before the piece type ones,
    * so we need to prepare an offset.
    */
  val PieceTypeOffset = 2

  val KingSideCastlingIndex = 1
  val QueenSideCastlingIndex = 2

  def apply(): Bitboard = {
    // Initialize the white pieces
    val partialBitboard = Bitboard(Vector.fill(Board.Size)(0), Vector(), None)
      .updatePiece(whiteOf(Pawn)) {
        _ | toBitset(A(_2), B(_2), C(_2), D(_2), E(_2), F(_2), G(_2), H(_2))
      }.updatePiece(whiteOf(Knight)) { _ | toBitset(B(_1), G(_1)) }
      .updatePiece(whiteOf(Bishop))  { _ | toBitset(C(_1), F(_1)) }
      .updatePiece(whiteOf(Rook))    { _ | toBitset(A(_1), H(_1)) }
      .updatePiece(whiteOf(Queen))   { _ | singleBitset(D(_1)) }
      .updatePiece(whiteOf(King))    { _ | singleBitset(E(_1)) }

    /**
      * Rotate each of the white piece positions to get the
      * corresponding black piece positions.
      */
    val fullBitboard = (PieceTypeOffset until Board.Size).foldLeft(partialBitboard) { (bitboard, i) =>
      val pieceTypeBitset = bitboard.bitsets(i)
      bitboard.updatePiece(blackOf(i - PieceTypeOffset)) {
        _ | Transformers.rotate180(pieceTypeBitset)
      }
    }

    // Swap the positions of the black king and the black queen
    val toggleKingQueen: U64 => U64 = _ ^ toBitset(D(_8), E(_8))
    fullBitboard.updatePiece(blackOf(Queen))(toggleKingQueen)
      .updatePiece(blackOf(King))(toggleKingQueen)

      // update the castling bitsets
      .withCastlingBitsets(Vector(
        singleBitset(E(_1)) | singleBitset(E(_8)),
        singleBitset(H(_1)) | singleBitset(H(_8)),
        singleBitset(A(_1)) | singleBitset(A(_8))))
  }

  def toBitPosition(location: Location): Int = location.file + location.rank * Board.Size

  def fileOf(position: Int) = position % Board.Size

  def rankOf(position: Int) = position / Board.Size

  def singleBitset(position: Int) = 1L << position

  def isEmptySet(bitboard: U64) = bitboard == 0

  def isNonEmptySet(bitboard: U64) = !isEmptySet(bitboard)

  def intersectedWith(bitset: U64)(x: U64): Boolean = isNonEmptySet(x & bitset)

  def leastSignificantOneBit(bitboard: U64) = bitboard & -bitboard

  /**
    * Retrieves the index of a 1 bit in a given bitboard.
    * It is assumed that the bitboard contains only one
    * 1 bit, and that the rest are zeroes.
    *
    * TODO: See algorithms for Bit scan forward for a more
    * efficient approach. Also, change the name to bitScanForward.
    */
  def oneBitIndex(bitset: U64) = {
    @tailrec
    def recurse(ls1b: Long, i: Int): Int =
      if (isNonEmptySet(ls1b)) recurse(ls1b >>> 1, i + 1)
      else i

    recurse(leastSignificantOneBit(bitset), -1)
  }

  def isolate(bitset: U64): Stream[U64] = {
    @tailrec
    def recurse(bitset: U64, bitsets: Stream[U64]): Stream[U64] =
      if (isNonEmptySet(bitset))
        recurse(bitset & bitset - 1, leastSignificantOneBit(bitset) #:: bitsets)
      else bitsets

    recurse(bitset, Stream())
  }

  def count(bitset: U64) = isolate(bitset).size

  def toSquareIndexes: U64 => Stream[Int] = isolate(_).map(oneBitIndex)

  def toBitset(positions: Int*): U64 = positions.foldLeft(0L) { (bitset, position) =>
    bitset | singleBitset(position)
  }
}

case class Bitboard(bitsets: Vector[U64],
  castlingBitsets: Vector[U64],
  lastBitboardMove: Option[BitboardMove],
  enPassantBitset: U64 = 0L) extends Board {

  import Bitboard._

  lazy val (sideBitsets, pieceTypeBitsets) = bitsets.splitAt(PieceTypeOffset)

  def updatePiece(piece: Piece)(f: U64 => U64): Bitboard = piece match {
    case Piece(pieceType, side) =>
      val pieceTypeIndex = pieceType + PieceTypeOffset
      val updatedPieceType = bitsets.updated(pieceTypeIndex, f(bitsets(pieceTypeIndex)))
      val updateBitSets = updatedPieceType.updated(side, f(updatedPieceType(side)))
      Bitboard(updateBitSets, castlingBitsets, lastBitboardMove)
  }

  override def updateByMove(move: LocationMove, piece: Piece) =
    updateByBitboardMove(Move[Int](move.source, move.destination, move.moveType), piece)

  def updateByBitboardMove(move: BitboardMove, piece: Piece): Bitboard = {
    val sourceBitset = singleBitset(move.source)
    val destBitset = singleBitset(move.destination)
    val moveBitset = sourceBitset ^ destBitset

    // handle captures
    val oppositeSide = piece.side.opposite
    val oppositeSideBitset = sideBitsets(oppositeSide)
    val capturedIndex = pieceTypeBitsets.indexWhere { bitset =>
      val oppositePieceTypeBitset = bitset & oppositeSideBitset
      isNonEmptySet(oppositePieceTypeBitset & destBitset)
    }

    val captureBoard =
      if (capturedIndex == -1) this
      else updatePiece(Piece(capturedIndex, oppositeSide))( _ ^ destBitset)

    val partialBoard = captureBoard.updatePiece(piece)(_ ^ moveBitset).updateLastMove(move)
      .withCastlingBitsets(castlingBitsets.map { bitset =>
        if (isNonEmptySet(bitset & sourceBitset)) bitset ^ sourceBitset
        else bitset
      })

    // handle special cases
    move.moveType match {
      case EnPassant =>
        partialBoard.updatePiece(Piece(Pawn, oppositeSide)) { _ ^ {
          if (piece.side == White) destBitset >> Board.Size
          else destBitset << Board.Size
        }}
      case PawnPromotion(promotionPiece) =>
        // remove the pawn and replace it with the specified officer
        partialBoard.updatePiece(piece)( _ ^ destBitset)
          .updatePiece(promotionPiece)(_ ^ destBitset)
      case DoublePawnPush => partialBoard.withEnPassantBitset(destBitset)
      case Castling => partialBoard.castle(move, piece.side)
      case _ => partialBoard
    }
  }

  def castle(kingMove: BitboardMove, side: Side) = {
    val index = castlingRookIndex(kingMove, side)

    val updatedBoard = updatePiece(Piece(Rook, side)) { rookBitset =>
      val castlingRookBitset = castlingBitsets(index) & sideBitsets(side)
      val movedCastlingRookBitset =
        if (index == KingSideCastlingIndex) castlingRookBitset >>> 2
        else castlingRookBitset << 3

      rookBitset ^ castlingRookBitset | movedCastlingRookBitset
    }

    val updatedCastlingBitsets = castlingBitsets
      // remove the king
      .updated(0, castlingBitsets(0) & pieceTypeBitsets(King))

      // remove the rook
      .updated(index, castlingBitsets(index) & updatedBoard.pieceTypeBitsets(Rook))

    updatedBoard.withCastlingBitsets(updatedCastlingBitsets)
  }

  def updateLastMove(move: BitboardMove) = Bitboard(bitsets, castlingBitsets, Some(move))

  def withCastlingBitsets(castlingBitsets: Vector[U64]) =
    Bitboard(bitsets, castlingBitsets, lastBitboardMove)

  def withEnPassantBitset(enPassantBitset: U64) =
    Bitboard(bitsets, castlingBitsets, lastBitboardMove, enPassantBitset)

  def at(position: Int): Option[Piece] = at(Bitboard.singleBitset(position))

  def at(bitset: U64): Option[Piece] = {
    val sideIndex = sideBitsets.indexWhere(intersectedWith(bitset))
    if (sideIndex == -1) None
    else {
      val pieceIndex = pieceTypeBitsets.indexWhere(intersectedWith(bitset))
      if (pieceIndex == -1) None
      else Some(Piece(pieceIndex, sideIndex))
    }
  }

  override def at(location: Location): Option[Piece] = at(toBitPosition(location))

  override def lastMove = lastBitboardMove.map { move =>
    Move[Location](move.source, move.destination, move.moveType)
  }

  override def locate(piece: Piece): List[Location] = bitPositions(piece).map(intToLocation)

  /**
    * Get the positions of a particular type (and color) of a piece.
    *
    * TODO: This may not be a very efficient approach. Consider optimizing this
    * if speed becomes an issue.
    */
  def bitPositions(piece: Piece): List[Int] = {
    @tailrec
    def recurse(bitboard: U64, acc: List[Int]): List[Int] =
      if (isEmptySet(bitboard)) acc
      else recurse(bitboard ^ leastSignificantOneBit(bitboard), oneBitIndex(bitboard) :: acc)

    recurse(pieceBitset(piece), Nil)
  }

  def pieceBitset: Piece => U64 = { case Piece(pieceType, side) =>
    sideBitsets(side) & pieceTypeBitsets(pieceType)
  }

  def apply: Int => Option[Piece] = at

  def whitePieces = bitsets(White)
  def blackPieces = bitsets(Black)

  def occupied = whitePieces | blackPieces
  def emptySquares = ~occupied
  def opponents(side: Side) = bitsets(side.opposite)

  override def isChecked(side: Side) = {
    val kingPosition = oneBitIndex(pieceBitset(side.of(King)))
    val moveGenerators = (PawnMoveGenerator, Pawn :: Nil) ::
      (KnightMoveGenerator, Knight :: Nil) ::
      (BishopMoveGenerator, Bishop :: Queen :: Nil) ::
      (RookMoveGenerator, Rook :: Queen :: Nil) ::
      (KingMoveGenerator, King :: Nil) :: Nil

    moveGenerators.exists { case (generator, pieceTypes) =>
      generator.attackBitsets(this, kingPosition, side) exists { destination =>
        at(destination).exists { case Piece(destType, destSide) =>
          pieceTypes.contains(destType) && destSide == side.opposite
        }
      }
    }
  }

  override def isCheckmate(winningSide:Side) = {
    val loosingSide = winningSide.opposite
    val piecesToMove = Pawn :: Knight :: Bishop :: Rook :: Queen :: King :: Nil

    !piecesToMove.exists { pieceType =>
      val piece = Piece(pieceType, loosingSide)
      val pieceIndexes = toSquareIndexes(pieceBitset(piece))
      pieceIndexes.exists { squareIndex =>
        val moveGenerator = BitboardMoveGenerator.moveGenerator(pieceType)
        moveGenerator.validMoves(this, squareIndex, loosingSide)
          .exists(!updateByBitboardMove(_, piece).isChecked(loosingSide))
      }
    }
  }

  override def canCastle(kingMove: LocationMove) = at(kingMove.source) exists {
    case piece@Piece(King, side) =>
      val kingBitset = pieceBitset(piece)
      val sideBitset = sideBitsets(side)

      val kingHasBeenMoved = isEmptySet(castlingBitsets(0) & kingBitset)
      val rookHasBeenMoved = {
        val kingBitboardMove = Move[Int](kingMove.source, kingMove.destination)
        val index = castlingRookIndex(kingBitboardMove, side)
        isEmptySet(castlingBitsets(index) & sideBitset)
      }
      !kingHasBeenMoved && !rookHasBeenMoved
    case _ => false
  }

  def castlingRookIndex(kingMove: BitboardMove, side: Side) = {
    val delta = kingMove.destination.file - kingMove.source.file
    if (delta < 0) QueenSideCastlingIndex else KingSideCastlingIndex
  }

  override def generateMoves(sideToMove: Side) = types.toStream.flatMap { pieceType =>
    val moveGenerator = BitboardMoveGenerator.moveGenerator(pieceType)
    val piece = Piece(pieceType, sideToMove)
    val piecePositions = toSquareIndexes(pieceBitset(piece))
    piecePositions.flatMap { source =>
      val moves = moveGenerator.validMoves(this, source, sideToMove)
      moves.map(move => (Move.transform(intToLocation)(move), piece))
    }
  }

  override def updateByNextMove(sideToMove: Side) =
    AlphaBetaMax.search(this, sideToMove, -Integer.MAX_VALUE, Integer.MAX_VALUE, AlphaBeta.DefaultMaxDepth)._2
}
