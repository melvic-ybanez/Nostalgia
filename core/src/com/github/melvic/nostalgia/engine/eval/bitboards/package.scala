package com.github.melvic.nostalgia.engine.eval

import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard
import com.github.melvic.nostalgia.engine.eval.{Evaluator => BaseEvaluator}

package object bitboards {
  type Evaluator = BaseEvaluator[Bitboard, Int, Int, Int]
}
