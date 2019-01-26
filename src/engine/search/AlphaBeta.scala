package engine.search

import engine.board.{Board, Piece}
import engine.movegen.Move.LocationMove

import scala.annotation.tailrec

/**
  * Created by melvic on 1/27/19.
  */
sealed trait AlphaBeta {

  /**
    * Represents the alpha (lower) and beta (upper) bounds.
    */
  case class Bounds(alpha: Int, beta: Int)

  def evaluate(board: Board): Int

  /**
    * Determines whether a cut-off is required or not.
    * @return An optional integer, denoting a bound.
    */
  def whenCutOff(score: Int)(implicit bounds: Bounds): Option[Int]

  def isBetterScore(score: Int)(implicit bounds: Bounds): Boolean

  def initialBestScore(implicit bounds: Bounds): Int

  /**
    * Recursively evaluates a given board using the Alpha-Beta Pruning algorithm.
    * @param board board to evaluate
    * @param depth remaining depth in the search tree
    */
  def evaluate(board: Board, depth: Int)(implicit bounds: Bounds): Int =
    if (depth == 0) evaluate(board)
    else {
      @tailrec
      def recurse(bestScore: Int, moves: Stream[(LocationMove, Piece)]): Int = moves match {
        case Stream() => bestScore
        case (move, piece) +: nextMoves =>
          val updatedBoard = board.updateByMove(move, piece)
          val score = evaluate(updatedBoard, depth - 1)
          val cutOffScore = whenCutOff(score)

          if (cutOffScore.isDefined) cutOffScore.get
          else if (isBetterScore(score)) recurse(score, nextMoves)
          else recurse(bestScore, nextMoves)
      }

      recurse(initialBestScore, board.generateMoves)
    }
}

class AlphaBetaMax extends AlphaBeta {
  override def whenCutOff(score: Int)(implicit bounds: Bounds) =
    if (score >= bounds.beta) Some(bounds.beta) else None

  override def evaluate(board: Board) = board.evaluate

  override def isBetterScore(score: Int)(implicit bounds: Bounds) = score > bounds.alpha

  override def initialBestScore(implicit bounds: Bounds) = bounds.alpha
}

class AlphaBetaMin extends AlphaBeta {
  override def evaluate(board: Board) = -board.evaluate

  override def whenCutOff(score: Int)(implicit bounds: Bounds) =
    if (score <= bounds.alpha) Some(bounds.alpha) else None

  override def isBetterScore(score: Int)(implicit bounds: Bounds) = score < bounds.beta

  override def initialBestScore(implicit bounds: Bounds) = bounds.beta
}
