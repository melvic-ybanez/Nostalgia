package com.github.melvic.nostalgia.engine.eval

import com.github.melvic.nostalgia.engine.base.Side
import com.github.melvic.nostalgia.engine.board.bitboards.{BBBoard, Bitboard}
import com.github.melvic.nostalgia.engine.board.Board
import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard.Bitboard

/**
  * Created by melvic on 2/4/19.
  */
trait Evaluator[B, T, S, L] {
  implicit val board: Board[B, T, S, L]

  def evaluate(board: B, sideToMove: S): Double
}

object Evaluator {
  def apply[B, T, S, L](implicit E: Evaluator[B, T, S, L]): Evaluator[B, T, S, L] = E

  implicit val bitboardEvaluator: BBEvaluator = new BBEvaluator {
    implicit val board: BBBoard = Board[Bitboard, Int, Int, Int]

    override def evaluate(board: Bitboard, sideToMove: Int): Double =
      BitboardEvaluator(board, sideToMove).evaluate
  }
}
