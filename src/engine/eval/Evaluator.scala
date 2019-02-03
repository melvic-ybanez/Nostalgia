package engine.eval

import engine.board.{Board, Side}
import engine.board.bitboards.Bitboard

/**
  * Created by melvic on 2/4/19.
  */
object Evaluator {
  def evaluate(board: Board, sideToMove: Side): Int = board match {
    case bitboard@Bitboard(_, _, _) => BitboardEvaluator(bitboard).evaluate(sideToMove)
    case _ => 0
  }
}
