package com.github.melvic.nostalgia.engine.eval

import com.github.melvic.nostalgia.engine.base.{Board, Side}
import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard
import com.github.melvic.nostalgia.engine.eval.bitboards.EvalInstance

/**
  * Created by melvic on 2/4/19.
  */
trait Evaluator[B, T, S, L] {
  implicit val board: Board[B, T, S, L]

  def evaluate(board: B, sideToMove: S): Double
}

object Evaluator {
  def apply[B, T, S, L](implicit E: Evaluator[B, T, S, L]): Evaluator[B, T, S, L] = E
}
