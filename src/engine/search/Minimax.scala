package engine.search

import engine.board.Board

/**
  * Created by melvic on 1/26/19.
  */
sealed trait Minimax {
  def comparisonScore: Int => Int
  def evaluate: Board => Int

  def evaluate(depth: Int, board: Board): Int = {
    def minimax(depth: Int, board: Board, max: Minimax, min: Minimax): Int =
      if (depth == 0) max.evaluate(board)
      else board.generateMoves.map { case (move, piece) =>
        val movedBoard = board.updateByMove(move, piece)
        val score = minimax(depth - 1, movedBoard, min, max)
        comparisonScore(score)
      }.max

    minimax(depth, board, new Max, new Min)
  }
}

class Max extends Minimax {
  override def evaluate = _.evaluate
  override def comparisonScore = identity
}

class Min extends Minimax {
  override def evaluate = -_.evaluate
  override def comparisonScore = -_
}
