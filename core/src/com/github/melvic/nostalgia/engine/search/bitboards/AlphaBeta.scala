package com.github.melvic.nostalgia.engine.search.bitboards

import com.github.melvic.nostalgia.engine.base.Side
import com.github.melvic.nostalgia.engine.board.Board
import com.github.melvic.nostalgia.engine.board.bitboards._
import com.github.melvic.nostalgia.engine.eval.bitboards.Evaluator
import com.github.melvic.nostalgia.engine.search.{AlphaBeta => BaseAlphaBeta}

trait AlphaBeta {
  implicit val board: Bitboard = Board[BitboardInstance, Int, Int, Int]

  implicit def side: Side[Int] = Side[Int]

  implicit val evaluator: Evaluator = Evaluator[BitboardInstance, Int, Int, Int]
}
