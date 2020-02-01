package com.github.melvic.nostalgia.engine.movegen.bitboards

import com.github.melvic.nostalgia.engine.board.Piece._
import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard
import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard.U64
import com.github.melvic.nostalgia.engine.board.{King, Piece}
import com.github.melvic.nostalgia.engine.movegen.bitboards.OneStep.Step
import com.github.melvic.nostalgia.engine.movegen.{Castling, Move, Normal}

/**
  * Created by melvic on 9/23/18.
  */
object KingMoveGenerator extends NonSlidingMoveGenerator with PostShiftOneStep {
  override lazy val moves: Stream[Step] = Stream(
    north, south, east, west, northEast, northWest, southEast, southWest
  )

  lazy val castlingDestinationBitsets: StreamGen[WithMove[U64]] = { (board, source, side) =>
    val kingBitset = Bitboard.singleBitset(source)

    def castle(step: Step, rookIndex: Int, rookSteps: U64): U64 = {
      val kingCastlingBitset = board.castlingBitsets(0) & board.sideBitsets(side)
      val rookCastlingBitset = board.castlingBitsets(rookIndex) & board.sideBitsets(side)
      val castlingBitset = kingCastlingBitset | rookCastlingBitset

      val sourceCastlingBitset = kingBitset | rookSteps

      if (Bitboard.isEmptySet(sourceCastlingBitset & castlingBitset)) 0L
      else {
        val kingFirstStep = step(kingBitset)
        if (Bitboard.isNonEmptySet(kingFirstStep)) 0L
        else {
          val destination = Bitboard.bitScan(kingFirstStep)
          val move = Move[Int](source, destination, Normal)
          val updatedMove = board.updateByBitboardMove(move, Piece(King, side))

          if (updatedMove.isChecked(side)) 0L
          else step(kingFirstStep) // return second step
        }
      }
    }

    Stream(
      (castle(east, Bitboard.KingSideCastlingIndex, kingBitset << 3), Castling),
      (castle(west, Bitboard.QueenSideCastlingIndex, kingBitset >>> 4), Castling)
    )
  }

  override def destinationBitsets = { (board, source, side) =>
    super.destinationBitsets(board, source, side) ++ castlingDestinationBitsets(board, source, side)
  }
}
