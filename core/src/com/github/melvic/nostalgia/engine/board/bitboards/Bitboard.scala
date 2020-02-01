package com.github.melvic.nostalgia.engine.board.bitboards

import com.github.melvic.nostalgia.engine.board
import com.github.melvic.nostalgia.engine.board.Piece._
import com.github.melvic.nostalgia.engine.board._
import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard.U64
import com.github.melvic.nostalgia.engine.movegen.Location._
import com.github.melvic.nostalgia.engine.movegen.Move.{BitboardMove, LocationMove}
import com.github.melvic.nostalgia.engine.movegen.bitboards.BitboardMoveGenerator
import com.github.melvic.nostalgia.engine.movegen.{bitboards => _, _}
import com.github.melvic.nostalgia.engine.search.AlphaBetaMax

import scala.annotation.tailrec

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

  val debruijn = 0x03f79d71b4cb0a89L

  lazy val debruijnTable: Map[U64, Int] = {
    (0 until 64).foldLeft(Map[U64, Int]()) { (index64, i) =>
      val index = (debruijn << i) >>> 58
      index64 + (index -> i)
    }
  }

  def apply(): Bitboard = {
    // Initialize the white pieces
    val partialBitboard = bitboards.Bitboard(Vector.fill(Board.Size)(0), Vector(), None)
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
    * Returns the position of the least significant 1 bit. It is assumed that
    * the board contains only one 1 bit.
    */
  def bitScan(bitset: U64) = {
    val ls1b = leastSignificantOneBit(bitset)
    val shiftedLeft = ls1b * debruijn
    val index = shiftedLeft >>> 58
    debruijnTable(index)
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

  def toSquareIndexes: U64 => Stream[Int] = isolate(_).map(bitScan)

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
      bitboards.Bitboard(updateBitSets, castlingBitsets, lastBitboardMove)
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
      else updatePiece(board.Piece(capturedIndex, oppositeSide))( _ ^ destBitset)

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

  def updateLastMove(move: BitboardMove) = bitboards.Bitboard(bitsets, castlingBitsets, Some(move))

  def withCastlingBitsets(castlingBitsets: Vector[U64]) =
    bitboards.Bitboard(bitsets, castlingBitsets, lastBitboardMove)

  def withEnPassantBitset(enPassantBitset: U64) =
    bitboards.Bitboard(bitsets, castlingBitsets, lastBitboardMove, enPassantBitset)

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

  override def isCheckmate(winningSide:Side) = {
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

  override def updateByNextMove(sideToMove: Side, depth: Int) =
    AlphaBetaMax.search(this, sideToMove, -Integer.MAX_VALUE, Integer.MAX_VALUE, depth)._2

  override def pieceLocations(piece: Piece) =
    toSquareIndexes(pieceBitset(piece)).map(intToLocation)

  override def toString = {
    def sideString: Side => String = List("W", "B")(_)
    def pieceTypeString: PieceType => String = "PNBRQK"(_).toString
    val space = " " * 2

    val squareStrings = (0 until 64).foldLeft[List[String]](Nil) { (acc, i) =>
      val squareString = this(i).map { case Piece(pieceType, side) =>
        sideString(side) + pieceTypeString(pieceType)
      } getOrElse ("_" * space.length)

      if (i % Board.Size == 0) squareString :: acc
      else acc.head + space + squareString :: acc.tail
    }

    squareStrings.mkString("\n")
  }
}
