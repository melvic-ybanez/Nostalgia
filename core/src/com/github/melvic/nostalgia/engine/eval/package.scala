package com.github.melvic.nostalgia.engine

import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard.Bitboard

package object eval {
  type BBEvaluator = Evaluator[Bitboard, Int, Int, Int]
}
