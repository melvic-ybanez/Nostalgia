package engine.movegen.bitboards

import engine.board.{King, Piece}
import engine.board.bitboards.Bitboard
import engine.board.bitboards.Bitboard.U64
import engine.board.Piece._
import engine.movegen.bitboards.OneStep.Step
import engine.movegen._

/**
  * Created by melvic on 9/23/18.
  */
object KingMoveGenerator extends NonSlidingMoveGenerator with PostShiftOneStep {
  override lazy val moves: Stream[Step] = Stream(
    north, south, east, west, northEast, northWest, southEast, southWest
  )

  lazy val castlingDestinationBitsets: StreamGen[WithMove[U64]] = { (board, source, side) =>
    val kingBitset = Bitboard.singleBitset(source)

    def castle(step: Step, rookIndex: Int, rookSteps: => U64): U64 = {
      val kingCastlingBitset = board.castlingBitsets(0) & board.sideBitsets(side)
      val rookCastlingBitset = board.castlingBitsets(rookIndex) & board.sideBitsets(side)
      val castlingBitset = kingCastlingBitset | rookCastlingBitset

      val sourceCastlingBitset = kingBitset | rookSteps

      if (Bitboard.isEmptySet(sourceCastlingBitset & castlingBitset)) 0L
      else {
        val kingFirstStep = step(kingBitset)
        val destination = Bitboard.oneBitIndex(kingFirstStep)
        val move = Move[Int](source, destination, Normal)
        val updatedMove = board.updateByBitboardMove(move, Piece(King, side))

        if (updatedMove.isChecked(side)) 0L
        else step(kingFirstStep)    // return second step
      }
    }

    Stream(
      (castle(east, Bitboard.KingSideCastlingIndex, kingBitset >>> 2), Castling),
      (castle(west, Bitboard.QueenSideCastlingIndex, kingBitset << 3), Castling)
    )
  }

  override def destinationBitsets = { (board, source, side) =>
    super.destinationBitsets(board, source, side) ++ castlingDestinationBitsets(board, source, side)
  }
}
