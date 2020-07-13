package com.github.melvic.nostalgia.engine.board.bitboards

import com.github.melvic.nostalgia.engine.base.Square
import com.github.melvic.nostalgia.engine.board.Board
import com.github.melvic.nostalgia.engine.board.bitboards.Piece._
import com.github.melvic.nostalgia.engine.movegen.Location._
import com.github.melvic.nostalgia.engine.movegen.{bitboards => _, _}

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

  def apply(): BitboardInstance = {
    // Initialize the white pieces
    val partialBitboard = BitboardInstance(Vector.fill(Board.Size)(0), Vector(), None)
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
