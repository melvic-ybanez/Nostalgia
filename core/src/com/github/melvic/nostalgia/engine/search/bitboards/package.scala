package com.github.melvic.nostalgia.engine.search

import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard
import com.github.melvic.nostalgia.engine.search.{
  AlphaBetaMax => BaseAlphaBetaMax,
  AlphaBetaMin => BaseAlphaBetaMin
}

package object bitboards extends AlphaBetaInstances {
  type AlphaBetaMax = BaseAlphaBetaMax[Bitboard, Int, Int, Int]
  type AlphaBetaMin = BaseAlphaBetaMin[Bitboard, Int, Int, Int]
}
