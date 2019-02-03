package engine.search

import engine.board.{Board, Piece, Side}
import engine.movegen.Move.LocationMove

import scala.annotation.tailrec

/**
  * Created by melvic on 1/27/19.
  */
sealed trait AlphaBeta {
  def evaluateBoard(board: Board): Int

  def cutOffBound(score: Int, bound: Int): Boolean

  def isBetterScore(score: Int, bestScore: Int): Boolean

  def opponent: AlphaBeta

  /**
    * Recursively evaluates a given board using the Alpha-Beta Pruning algorithm.
    * @param board board to evaluate
    * @param depth remaining depth in the search tree
    * @return A pair consisting of the evaluation score and the chosen next move.
    */
  def move(board: Board, sideToMove: Side, currentScore: Int, bound: Int, depth: Int): (Int, Board) =
    if (depth == 0) (evaluateBoard(board), board)
    else {
      @tailrec
      def recurse(bestScore: Int, board: Board, moves: Stream[(LocationMove, Piece)]): (Int, Board) = moves match {
        case Stream() => (bestScore, board)
        case (move, piece) +: nextMoves =>
          val updatedBoard = board.updateByMove(move, piece)
          val (score, _) = opponent.move(updatedBoard, sideToMove.opposite, bestScore, bound,depth - 1)

          if (cutOffBound(score, bound)) (bound, updatedBoard)
          else {
            val newBoard = if (isBetterScore(score, bestScore)) updatedBoard else board
            recurse(score, newBoard, nextMoves)
          }
      }

      recurse(currentScore, board, board.generateMoves(sideToMove))
    }
}

object AlphaBetaMax extends AlphaBeta {
  override def cutOffBound(score: Int, bound: Int) = score >= bound

  override def evaluateBoard(board: Board) = board.evaluate

  override def isBetterScore(score: Int, bestScore: Int) = score > bestScore

  override def opponent = AlphaBetaMin
}

object AlphaBetaMin extends AlphaBeta {
  override def evaluateBoard(board: Board) = -board.evaluate

  override def cutOffBound(score: Int, bound: Int) = score <= bound

  override def isBetterScore(score: Int, bestScore: Int) = score < bestScore

  override def opponent = AlphaBetaMax
}
