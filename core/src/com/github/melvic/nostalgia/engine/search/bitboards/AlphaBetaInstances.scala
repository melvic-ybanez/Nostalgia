package com.github.melvic.nostalgia.engine.search.bitboards

trait implicits {
  implicit object AlphaBetaMax extends AlphaBetaMax(AlphaBetaMin) with AlphaBeta

  implicit object AlphaBetaMin extends AlphaBetaMin(AlphaBetaMax) with AlphaBeta
}

object implicits extends implicits
