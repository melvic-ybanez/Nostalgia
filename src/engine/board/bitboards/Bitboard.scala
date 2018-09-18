package engine.board.bitboards

import engine.board.Piece._
import engine.board.bitboards.Bitboard.{PieceTypeOffset, U64}
import engine.movegen.{EnPassant, Location, Move, PawnPromotion}
import engine.movegen.Location._
import engine.movegen.Move.{BitboardMove, LocationMove}

import scala.annotation.tailrec
import engine.board._
import Bitboard._

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

  def apply(): Bitboard = {
    // Initialize the white pieces
    val partialBitboard = Bitboard(Array.fill(Board.Size)(0), None)
      .updatePiece(whiteOf(Pawn), _ | 0x000000000000ff00L)
      .updatePiece(whiteOf(Knight), _ | 0x0000000000000042L)
      .updatePiece(whiteOf(Bishop), _ | 0x0000000000000024L)
      .updatePiece(whiteOf(Rook), _ | 0x0000000000000081L)
      .updatePiece(whiteOf(Queen), _ | 0x0000000000000008L)
      .updatePiece(whiteOf(King), _ | 0x0000000000000010L)

    /**
      * Rotate each of the white piece positions to get the
      * corresponding black piece positions.
      */
    @tailrec
    def rotate(bitboard: Bitboard, i: Int): Bitboard =
      if (i == Board.Size) bitboard
      else {
        val pieceTypeBitset = bitboard.bitsets(i)
        val newBitboard = bitboard.updatePiece(blackOf(i - PieceTypeOffset),
          _ | Transformers.rotate180(pieceTypeBitset))
        rotate(newBitboard, i + 1)
      }

    val fullBitboard = rotate(partialBitboard, PieceTypeOffset)

    // Swap the positions of the black king and the black queen
    val toggleKingQueen: U64 => U64 = _ ^ 0x1800000000000000L
    fullBitboard.updatePiece(blackOf(Queen), toggleKingQueen)
      .updatePiece(blackOf(King), toggleKingQueen)
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
    * Retrieve the index of a 1 bit in a given bitboard.
    * It is assumed that the bitboard contains only one
    * 1 bit, and that the rest are zeroes.
    */
  def oneBitIndex(bitboard: U64) = {
    @tailrec
    def recurse(ls1b: Long, i: Int): Int =
      if (Bitboard.isNonEmptySet(ls1b)) recurse(ls1b >>> 1, i + 1)
      else i

    recurse(leastSignificantOneBit(bitboard), -1)
  }
}

case class Bitboard(bitsets: Array[U64], lastBitboardMove: Option[BitboardMove]) extends Board {
  lazy val (sideBitsets, pieceTypeBitsets) = bitsets.splitAt(PieceTypeOffset)

  def updatePiece(piece: Piece, f: U64 => U64): Bitboard = piece match {
    case Piece(pieceType, side) =>
      val pieceTypeIndex = pieceType + PieceTypeOffset
      val updatedPieceType = bitsets.updated(pieceTypeIndex, f(bitsets(pieceTypeIndex)))
      val updateBitSets = updatedPieceType.updated(side, f(updatedPieceType(side)))
      Bitboard(updateBitSets, lastBitboardMove)
  }

  override def updateByMove(move: LocationMove, piece: Piece) =
    updateByBitboardMove(Move[Int](move.source, move.destination, move.moveType), piece)

  def updateByBitboardMove(move: BitboardMove, piece: Piece): Board = {
    val sourceBitset = Bitboard.singleBitset(move.source)
    val destBitset = Bitboard.singleBitset(move.destination)
    val moveBitset = sourceBitset ^ destBitset

    // handle captures
    val oppositeSide = piece.side.opposite
    val oppositeSideBitset = sideBitsets(oppositeSide)
    val capturedIndex = pieceTypeBitsets.indexWhere { bitset =>
      val oppositePieceTypeBitset = bitset & oppositeSideBitset
      Bitboard.isNonEmptySet(oppositePieceTypeBitset & destBitset)
    }

    val captureBoard =
      if (capturedIndex == -1) this
      else updatePiece(Piece(capturedIndex, oppositeSide), _ ^ destBitset)

    val partialBoard = captureBoard.updatePiece(piece, _ ^ moveBitset).updateLastMove(move)

    // handle special cases
    move match {
      case Move(_, _, EnPassant) =>
        partialBoard.updatePiece(Piece(Pawn, oppositeSide), _ ^ {
          if (piece.side == White) destBitset >> Board.Size
          else destBitset << Board.Size
        })
      case Move(_, _, PawnPromotion(promotionPiece)) =>
        // remove the pawn and replace it with the specified officer
        partialBoard.updatePiece(piece, _ ^ destBitset)
          .updatePiece(promotionPiece, _ ^ destBitset)
      case _ => partialBoard
    }
  }

  def updateLastMove(move: BitboardMove) = Bitboard(bitsets, Some(move))

  def at(position: Int): Option[Piece] = {
    val bitset = Bitboard.singleBitset(position)
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

  def apply: Int => Option[Piece] = at

  def whitePieces = bitsets(White)
  def blackPieces = bitsets(White)
  def pawns = bitsets(Pawn)
  def knights = bitsets(Knight)
  def bishops = bitsets(Bishop)
  def rooks = bitsets(Rook)
  def queens = bitsets(Queen)
  def kings = bitsets(King)

  def occupied = whitePieces | blackPieces
  def emptySquares = ~occupied
  def opponents(side: Side) = bitsets(side.opposite)
}
