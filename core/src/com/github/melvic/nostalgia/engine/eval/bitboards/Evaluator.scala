package com.github.melvic.nostalgia.engine.eval.bitboards
import com.github.melvic.nostalgia.engine.board.bitboards._

object Evaluator {
  implicit val evaluator: Evaluator = new Evaluator {
    override implicit val board: Board = Board

    override def evaluate(board: Bitboard, sideToMove: Int) =
      EvalInstance(board, sideToMove).evaluate
  }
}
