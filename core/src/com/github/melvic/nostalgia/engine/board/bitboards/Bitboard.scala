package com.github.melvic.nostalgia.engine.board.bitboards

import com.github.melvic.nostalgia.engine.base.MoveType.{Castling, DoublePawnPush, EnPassant, PawnPromotion}
import com.github.melvic.nostalgia.engine.base.Square
import com.github.melvic.nostalgia.engine.board.Board
import com.github.melvic.nostalgia.engine.board.bitboards.Piece._
import com.github.melvic.nostalgia.engine.movegen.Location._
import com.github.melvic.nostalgia.engine.movegen.bitboards.BitboardMoveGenerator
import com.github.melvic.nostalgia.engine.movegen.{bitboards => _, _}
import com.github.melvic.nostalgia.engine.search.AlphaBetaMax
import com.github.melvic.nostalgia.engine.search.bitboards.AlphaBetaInstances.AlphaBetaMax

import scala.annotation.tailrec

/**
  * Created by melvic on 8/5/18.
  */
object Bitboard {
  type U64 = Long
  type SetwiseOp = (U64, Int) => U64
  type SplitBoard[A] = U64 => List[A]

  /**
   * The side-to-move bitboards come first before the piece type ones,
   * so we need to prepare an offset.
   */
  val PieceTypeOffset = 2

  val KingSideCastlingIndex = 1
  val QueenSideCastlingIndex = 2

  val debruijn = 0x03f79d71b4cb0a89L

  lazy val debruijnTable: Map[U64, Int] = {
    (0 until 64).foldLeft(Map[U64, Int]()) { (index64, i) =>
      val index = (debruijn << i) >>> 58
      index64 + (index -> i)
    }
  }

  def apply(): Bitboard = {
    // Initialize the white pieces
    val partialBitboard = Bitboard(Vector.fill(Board.Size)(0), Vector(), None)
      .updatePiece(white(Pawn)) {
        _ | toBitset(A(_2), B(_2), C(_2), D(_2), E(_2), F(_2), G(_2), H(_2))
      }.updatePiece(white(Knight)) {
        _ | toBitset(B(_1), G(_1))
      }
      .updatePiece(white(Bishop)) {
        _ | toBitset(C(_1), F(_1))
      }
      .updatePiece(white(Rook)) {
        _ | toBitset(A(_1), H(_1))
      }
      .updatePiece(white(Queen)) {
        _ | singleBitset(D(_1))
      }
      .updatePiece(white(King)) {
        _ | singleBitset(E(_1))
      }

    /**
     * Rotate each of the white piece positions to get the
     * corresponding black piece positions.
     */
    val fullBitboard = (PieceTypeOffset until Board.Size).foldLeft(partialBitboard) { (bitboard, i) =>
      val pieceTypeBitset = bitboard.bitsets(i)
      bitboard.updatePiece(black(i - PieceTypeOffset)) {
        _ | Transformers.rotate180(pieceTypeBitset)
      }
    }

    // Swap the positions of the black king and the black queen
    val toggleKingQueen: U64 => U64 = _ ^ toBitset(D(_8), E(_8))
    fullBitboard.updatePiece(black(Queen))(toggleKingQueen)
      .updatePiece(black(King))(toggleKingQueen)

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
   * Returns the position of the least significant 1 bit. It is assumed that
   * the board contains only one 1 bit.
   */
  def bitScan(bitset: U64) = {
    val ls1b = leastSignificantOneBit(bitset)
    val shiftedLeft = ls1b * debruijn
    val index = shiftedLeft >>> 58
    debruijnTable(index)
  }

  def isolate: SplitBoard[U64] = { bitset =>
    @tailrec
    def recurse(bitset: U64, bitsets: List[U64]): List[U64] =
      if (isNonEmptySet(bitset))
        recurse(bitset & bitset - 1, leastSignificantOneBit(bitset) :: bitsets)
      else bitsets

    recurse(bitset, Nil)
  }

  def count(bitset: U64) = isolate(bitset).size

  def toSquareIndexes: SplitBoard[Int] = isolate(_).map(bitScan)

  def toBitset(positions: Int*): U64 = positions.foldLeft(0L) { (bitset, position) =>
    bitset | singleBitset(position)
  }

  implicit val bitboardBoard: Board = new Board {
    override implicit def square: Square[Int] = Square[Int]

    override def at(board: Bitboard, location: Int) = board.at(location)

    override def updateByMove(board: Bitboard, move: BMove, piece: BPiece) =
      board.updateByMove(move, piece)

    override def lastMove(board: Bitboard) = board.lastMove

    override def generateMoves(board: Bitboard, sideToMove: Int) =
      board.generateMoves(sideToMove)

    override def updateByNextMove(board: Bitboard, sideToMove: Int, depth: Int) =
      board.updateByNextMove(sideToMove, depth)

    override def isChecked(board: Bitboard, side: Int) = board.isChecked(side)

    override def isCheckmate(board: Bitboard, winningSide: Int) = board.isCheckmate(winningSide)

    override def canCastle(board: Bitboard, kingMove: BMove) = board.canCastle(kingMove)

    override def pieceLocations(board: Bitboard, piece: BPiece) = board.pieceLocations(piece)
  }
}

case class Bitboard(
    bitsets: Vector[U64],
    castlingBitsets: Vector[U64],
    lastBitboardMove: Option[Move],
    enPassantBitset: U64 = 0L) {

  import Bitboard._

  lazy val (sideBitsets, pieceTypeBitsets) = bitsets.splitAt(PieceTypeOffset)

  def updatePiece(piece: Piece)(f: U64 => U64): Bitboard = piece match {
    case Piece(pieceType, side) =>
      val pieceTypeIndex = pieceType + PieceTypeOffset
      val updatedPieceType = bitsets.updated(pieceTypeIndex, f(bitsets(pieceTypeIndex)))
      val updateBitSets = updatedPieceType.updated(side, f(updatedPieceType(side)))
      Bitboard(updateBitSets, castlingBitsets, lastBitboardMove)
  }

  def updateByMove(move: Move, piece: Piece) =
    updateByBitboardMove(Move(move.from, move.to, move.moveType), piece)

  def updateByBitboardMove(move: Move, piece: Piece): Bitboard = {
    val sourceBitset = singleBitset(move.from)
    val destBitset = singleBitset(move.to)
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

  def castle(kingMove: Move, side: Side) = {
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

  def updateLastMove(move: Move) = Bitboard(bitsets, castlingBitsets, Some(move))

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

  def lastMove = lastBitboardMove.map { move =>
    Move(move.from, move.to, move.moveType)
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

  def isChecked(side: Side) = {
    val kingPosition = bitScan(pieceBitset(side.of(King)))
    val moveGenerators = types.map(pieceType => (BitboardMoveGenerator.moveGenerator, pieceType))

    moveGenerators.exists { case (generatorType, attacker) =>
      val generator = generatorType(attacker)
      generator.attackBitsets(this, kingPosition, side) exists { destination =>
        at(destination).exists { case Piece(destType, destSide) =>
          attacker == destType && destSide == side.opposite
        }
      }
    }
  }

  def isCheckmate(winningSide:Side) = {
    val loosingSide = winningSide.opposite
    val piecesToMove = Pawn :: Knight :: Bishop :: Rook :: Queen :: King :: Nil

    isChecked(loosingSide) && !piecesToMove.exists { pieceType =>
      val piece = Piece(pieceType, loosingSide)
      val pieceIndexes = toSquareIndexes(pieceBitset(piece))
      pieceIndexes.exists { squareIndex =>
        val moveGenerator = BitboardMoveGenerator.moveGenerator(pieceType)
        moveGenerator.validMoves(this, squareIndex, loosingSide)
          .exists(!updateByBitboardMove(_, piece).isChecked(loosingSide))
      }
    }
  }

  def canCastle(kingMove: Move) = at(kingMove.from) exists {
    case piece@Piece(King, side) =>
      val kingBitset = pieceBitset(piece)
      val sideBitset = sideBitsets(side)

      val kingHasBeenMoved = isEmptySet(castlingBitsets(0) & kingBitset)
      val rookHasBeenMoved = {
        val kingBitboardMove = Move.normal(kingMove.from, kingMove.to)
        val index = castlingRookIndex(kingBitboardMove, side)
        isEmptySet(castlingBitsets(index) & sideBitset)
      }
      !kingHasBeenMoved && !rookHasBeenMoved
    case _ => false
  }

  def castlingRookIndex(kingMove: Move, side: Side) = {
    val delta = kingMove.to.file - kingMove.from.file
    if (delta < 0) QueenSideCastlingIndex else KingSideCastlingIndex
  }

  def generateMoves(sideToMove: Side) = types.flatMap { pieceType =>
    val moveGenerator = BitboardMoveGenerator.moveGenerator(pieceType)
    val piece = Piece(pieceType, sideToMove)
    val piecePositions = toSquareIndexes(pieceBitset(piece))
    piecePositions.flatMap { source =>
      val moves = moveGenerator.validMoves(this, source, sideToMove)
      moves.map(move => (move, piece))
    }
  }

  def updateByNextMove(sideToMove: Side, depth: Int) =
    AlphaBetaMax.search(this, sideToMove, -Integer.MAX_VALUE, Integer.MAX_VALUE, depth)._2

  def pieceLocations(piece: Piece) =
    toSquareIndexes(pieceBitset(piece))

  override def toString = {
    def sideString: Side => String = List("W", "B")(_)
    def pieceTypeString: PieceType => String = "PNBRQK"(_).toString
    val space = " " * 2

    val squareStrings = (0 until 64).foldLeft[List[String]](Nil) { (acc, i) =>
      val squareString = this.apply(i).map { case Piece(pieceType, side) =>
        sideString(side) + pieceTypeString(pieceType)
      } getOrElse ("_" * space.length)

      if (i % Board.Size == 0) squareString :: acc
      else acc.head + space + squareString :: acc.tail
    }

    squareStrings.mkString("\n")
  }
}
