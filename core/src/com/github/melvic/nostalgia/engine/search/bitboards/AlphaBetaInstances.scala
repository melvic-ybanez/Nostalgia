package com.github.melvic.nostalgia.engine.search.bitboards

trait AlphaBetaInstances {
  object AlphaBetaMax extends AlphaBetaMax(AlphaBetaMin) with AlphaBeta

  object AlphaBetaMin extends AlphaBetaMin(AlphaBetaMax) with AlphaBeta
}

object AlphaBetaInstances extends AlphaBetaInstances
