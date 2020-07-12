package com.github.melvic.nostalgia.engine.search.bitboards

import com.github.melvic.nostalgia.engine.base.Side
import com.github.melvic.nostalgia.engine.board.bitboards._
import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard.Bitboard
import com.github.melvic.nostalgia.engine.eval.{BBEvaluator, Evaluator}
import com.github.melvic.nostalgia.engine.search.{AlphaBeta => BaseAlphaBeta}

trait AlphaBeta extends BaseAlphaBeta[Bitboard, Int, Int, Int] {
  override implicit def side: Side[Int] = Side[Int]

  override implicit val evaluator: BBEvaluator = Evaluator[Bitboard, Int, Int, Int]
}
