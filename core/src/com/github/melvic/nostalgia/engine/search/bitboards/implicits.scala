package com.github.melvic.nostalgia.engine.search.bitboards

import com.github.melvic.nostalgia.engine.base.Side
import com.github.melvic.nostalgia.engine.board.Board
import com.github.melvic.nostalgia.engine.board.bitboards.BitboardInstance
import com.github.melvic.nostalgia.engine.eval.Evaluator
import com.github.melvic.nostalgia.engine.search.AlphaBeta.AlphaBetaMax

object AlphaBetaMax {
  implicit val bbAlphabetaMax: AlphaBetaMax = new AlphaBetaMax {
    override implicit val board: Board[BitboardInstance, Int, Int, Int] = _

    override implicit def side: Side[Int] = ???

    override implicit val evaluator: Evaluator[BitboardInstance, Int, Int, Int] = _
  }
}
