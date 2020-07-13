package com.github.melvic.nostalgia.engine.search

import com.github.melvic.nostalgia.engine.board.bitboards.BitboardInstance
import com.github.melvic.nostalgia.engine.search.{
  AlphaBetaMax => BaseAlphaBetaMax,
  AlphaBetaMin => BaseAlphaBetaMin
}

package object bitboards {
  type AlphaBetaMax = BaseAlphaBetaMax[BitboardInstance, Int, Int, Int]
  type AlphaBetaMin = BaseAlphaBetaMin[BitboardInstance, Int, Int, Int]
}
