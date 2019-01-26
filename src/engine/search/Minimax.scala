package engine.search

import engine.board.{Board, Piece}
import engine.movegen.Move.LocationMove

import scala.annotation.tailrec

/**
  * Created by melvic on 1/26/19.
  */
sealed trait Minimax {
  def init: Int
  def betterScore(score1: Int, score2: Int): Boolean
  def evaluate(board: Board): Int

  def search(depth: Int, board: Board, max: Minimax, min: Minimax): (Board, Int) =
    if (depth == 0) (board, evaluate(board))
    else {
      @tailrec
      def recurse(board: Board, bestScore: Int, boards: Stream[(LocationMove, Piece)]): (Board, Int) = boards match {
        case Seq() => (board, bestScore)
        case (move, piece) +: xs =>
          val movedBoard: Board = board.updateByMove(move, piece)
          val (newBoard, score) = max.search(depth - 1, movedBoard, min, max)

          // If the score is better than the current best, make it the new best.
          // Otherwise, keep the current best score.
          val (betterBoard, betterScore) =
            if (betterScore(score, bestScore)) (newBoard, score)
            else (board, bestScore)

          recurse(betterBoard, betterScore, xs)
      }

      recurse(board, init, board.generateMoves)
    }
}

class Max extends Minimax {
  override val init = -Integer.MAX_VALUE
  override def betterScore(score1: Int, score2: Int) = score1 > score2
  override def evaluate(board: Board) = board.evaluate
}

class Min extends Minimax {
  override val init = Integer.MAX_VALUE
  override def betterScore(score1: Int, score2: Int) = score1 < score2
  override def evaluate(board: Board) = -board.evaluate
}
