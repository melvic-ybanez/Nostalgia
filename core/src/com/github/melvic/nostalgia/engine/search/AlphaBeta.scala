package com.github.melvic.nostalgia.engine.search

import com.github.melvic.nostalgia.engine.base.Side
import com.github.melvic.nostalgia.engine.board.Board
import com.github.melvic.nostalgia.engine.eval.Evaluator

import scala.annotation.tailrec

/**
  * Created by melvic on 1/27/19.
  */
trait AlphaBeta[B, T, S, L] {

  implicit val board: Board[B, T, S, L]

  implicit def side: Side[S]

  implicit val evaluator: Evaluator[B, T, S, L]

  def evaluateBoard(board: B, side: S): Double

  def cutOffBound(score: Double, bound: Double): Boolean

  def isBetterScore(score: Double, bestScore: Double): Boolean

  def opponent: AlphaBeta[B, T, S, L]

  /**
    * Recursively evaluates a given board using the Alpha-Beta Pruning algorithm.
    * @param board board to evaluate
    * @param depth remaining depth in the search tree
    * @return A pair consisting of the evaluation score and the chosen next move.
    */
  def search(
      board: B,
      side: S,
      currentScore: Double,
      bound: Double,
      depth: Int
  ): (Double, B) = {
    lazy val result = (evaluateBoard(board, side), board)

    val boardTC = Board[B, T, S, L]

    if (depth == 0) result
    else {
      val validMoves = {
        // Apply all the moves
        val moves = boardTC.generateMoves(board, side).map { case (move, piece) =>
          boardTC.updateByMove(board, move, piece)
        }

        // Only include moves that do not leave the king checked
        moves.filter(board => boardTC.isChecked(board, side))
      }

      validMoves match {
        // This is a terminal node, return immediately
        case Nil => result

        case _ =>
          @tailrec
          def recurse(bestScore: Double, nextBoard: B, updatedBoards: List[B]): (Double, B) =
            updatedBoards match {
              case Nil => (bestScore, nextBoard)
              case updatedBoard +: nextMoves =>
                val (score, _) = opponent.search(
                  updatedBoard,
                  Side[S].opposite(side),
                  bound, bestScore,   // params positions switched
                  depth - 1
                )

                if (cutOffBound(score, bound)) (bound, updatedBoard)
                else if (isBetterScore(score, bestScore)) recurse(score, updatedBoard, nextMoves)
                else recurse(bestScore, nextBoard, nextMoves)
            }

          recurse(currentScore, board, validMoves)
      }
    }
  }
}

object AlphaBeta {
  val DefaultMaxDepth = 5
}
