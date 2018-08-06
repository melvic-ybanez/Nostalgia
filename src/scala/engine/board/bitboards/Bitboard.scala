package scala.engine.board.bitboards

import scala.engine.board.scala._
import scala.engine.board.scala.Piece._
import scala.engine.board.scala.bitboards.Bitboard.{PieceTypeOffset, U64}
import scala.engine.board.scala.bitboards.Implicits.Piece._
import scala.engine.board.scala.bitboards.Implicits.Location._
import scala.engine.movegen.scala.{BitboardMove, Location, Move}
import scala.annotation.tailrec
import scala.engine.board._

/**
  * Created by melvic on 8/5/18.
  */
object Bitboard {
  type U64 = Long
  type SetwiseOperator = (U64, Int) => U64

  /**
    * The side-to-move bitboards come first before the piece type ones,
    * so we need to prepare an offset.
    */
  val PieceTypeOffset = 2

  def initialize: Bitboard = {
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
      if (i == 6) bitboard
      else {
        val pieceType: PieceType = i
        val pieceTypeBitSet = bitboard.bitsets(pieceType)
        val newBitboard = bitboard.updatePiece(blackOf(pieceType),
          _ => Transformers.rotate180(pieceTypeBitSet))
        rotate(newBitboard, i - 1)
      }

    val fullBitboard = rotate(partialBitboard, 0)

    // Swap the positions of the black king and the black queen
    val toggleKingQueen: U64 => U64 = _ ^ 0x1800000000000000L
    fullBitboard.updatedBitset(Queen, toggleKingQueen)
      .updatedBitset(King, toggleKingQueen)
  }

  def toBitPosition(location: Location): Int = location.file + location.rank * Board.Size

  def singleBitset(position: Int) = 1L << position

  def isEmptySet(bitboard: U64) = bitboard == 0

  def isNonEmptySet(bitboard: U64) = !isEmptySet(bitboard)

  def intersectedWith(bitset: U64): U64 => Boolean = x => isNonEmptySet(x & bitset)

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

case class Bitboard(bitsets: Array[U64], optLastMove: Option[BitboardMove]) extends Board {
  def updatePiece(piece: Piece, f: U64 => U64): Bitboard = piece match {
    case Piece(pieceType, side) =>
      val updatedPieceType = bitsets.updated(pieceType, f(bitsets(pieceType)))
      val updateBitSets = updatedPieceType.updated(side, f(updatedPieceType(side)))
      Bitboard(updateBitSets, optLastMove)
  }

  def updatedBitset(i: Int, f: U64 => U64): Bitboard =
    Bitboard(bitsets.updated(i, f(bitsets(i))), optLastMove)

  def updateByMove(move: Move[Location], piece: Piece) =
    updateByBitboardMove(BitboardMove(move.source, move.destination, move.moveType), piece)

  def updateByBitboardMove(move: BitboardMove, piece: Piece): Board = {
    val sourceBitset = Bitboard.singleBitset(move.source)
    val destBitset = Bitboard.singleBitset(move.destination)
    val moveBitset = sourceBitset ^ destBitset

    // handle captures
    val oppositeSide = piece.side.opposite
    val oppositeSideBitset = bitsets(oppositeSide)
    val capturedIndex = bitsets.indexWhere { bitset =>
      val oppositePieceTypeBitset = bitset & oppositeSideBitset
      Bitboard.isNonEmptySet(oppositePieceTypeBitset & destBitset)
    }

    val newBoard =
      if (capturedIndex == -1) this
      else updatePiece(Piece(capturedIndex, oppositeSide), _ ^ destBitset)

    newBoard.updatePiece(piece, _ ^ moveBitset)
  }

  def at(position: Int): Piece = {
    val bitset = Bitboard.singleBitset(position)
    val side: Side = sideBitsets.indexWhere(Bitboard.intersectedWith(bitset))
    val pieceType: PieceType = pieceTypeBitsets.indexWhere(Bitboard.intersectedWith(bitset))
    Piece(pieceType, side)
  }

  def at(location: Location): Piece = at(locationToInt(location))

  def whitePieces = bitsets(White)
  def blackPieces = bitsets(White)

  lazy val (sideBitsets, pieceTypeBitsets) = bitsets.splitAt(PieceTypeOffset)

  def occupied = whitePieces | blackPieces
  def emptySquares = ~occupied
  def opponents(side: Side) = bitsets(side.opposite)
}
