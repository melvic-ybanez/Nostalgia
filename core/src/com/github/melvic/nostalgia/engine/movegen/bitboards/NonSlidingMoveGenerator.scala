package com.github.melvic.nostalgia.engine.movegen.bitboards

import com.github.melvic.nostalgia.engine.base.MoveType.Attack
import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard
import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard.U64

/**
  * Created by melvic on 8/5/18.
  */
trait NonSlidingMoveGenerator extends BitboardMoveGenerator {
  def moves: List[U64 => U64]

  def destinationBitsets: ListGen[WithMove[U64]] = { (board, source, side) =>
    val pieces = Bitboard.singleBitset(source)
    moves map { f =>
      val dest = emptyOrOpponent(board.emptySquares, board.opponents(side))(f(pieces))
      (dest, Attack)
    }
  }
}
