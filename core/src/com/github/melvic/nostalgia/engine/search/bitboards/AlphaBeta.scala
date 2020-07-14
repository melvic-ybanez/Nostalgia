package com.github.melvic.nostalgia.engine.search.bitboards

import com.github.melvic.nostalgia.engine.base.Side
import com.github.melvic.nostalgia.engine.board.bitboards._
import com.github.melvic.nostalgia.engine.eval.bitboards.Evaluator
import com.github.melvic.nostalgia.engine.search.{AlphaBeta => BaseAlphaBeta}

trait AlphaBeta {
  implicit val board: Board = Board

  implicit def side: Side[Int] = Side[Int]

  implicit val evaluator: Evaluator = Evaluator[Bitboard, Int, Int, Int]
}
