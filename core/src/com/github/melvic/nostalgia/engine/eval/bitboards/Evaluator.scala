package com.github.melvic.nostalgia.engine.eval.bitboards
import com.github.melvic.nostalgia.engine.board.Board
import com.github.melvic.nostalgia.engine.board.bitboards._

object Evaluator {
  implicit val evaluator: Evaluator = new Evaluator {
    override implicit val board: Bitboard =
      Board[BitboardInstance, Int, Int, Int]

    override def evaluate(board: BitboardInstance, sideToMove: Int) =
      EvalInstance(board, sideToMove).evaluate
  }
}
