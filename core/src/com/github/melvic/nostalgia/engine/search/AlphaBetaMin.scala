package com.github.melvic.nostalgia.engine.search

import com.github.melvic.nostalgia.engine.eval.Evaluator

abstract class AlphaBetaMin[B, T, S, L](max: AlphaBetaMax[B, T, S, L]) extends AlphaBeta[B, T, S, L] {
  override def evaluateBoard(board: B, side: S) = -Evaluator[B, T, S, L].evaluate(board, side)

  override def cutOffBound(score: Double, bound: Double) = score <= bound

  override def isBetterScore(score: Double, bestScore: Double) = score < bestScore

  override def opponent = max
}