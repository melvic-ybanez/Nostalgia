package com.github.melvic.nostalgia.engine.eval

import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard
import com.github.melvic.nostalgia.engine.board.{Board, Side}

/**
  * Created by melvic on 2/4/19.
  */
object Evaluator {
  def evaluate(board: Board, sideToMove: Side): Double = board match {
    case bitboard@Bitboard(_, _, _, _) => BitboardEvaluator(bitboard, sideToMove).evaluate
    case _ => 0
  }
}
