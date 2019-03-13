package ybanez.nostalgia.engine.eval

import ybanez.nostalgia.engine.board.{Board, Side}
import ybanez.nostalgia.engine.board.bitboards.Bitboard

/**
  * Created by melvic on 2/4/19.
  */
object Evaluator {
  def evaluate(board: Board, sideToMove: Side): Double = board match {
    case bitboard@Bitboard(_, _, _, _) => BitboardEvaluator(bitboard, sideToMove).evaluate
    case _ => 0
  }
}