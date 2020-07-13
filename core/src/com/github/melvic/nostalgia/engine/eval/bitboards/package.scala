package com.github.melvic.nostalgia.engine.eval

import com.github.melvic.nostalgia.engine.board.bitboards.BitboardInstance
import com.github.melvic.nostalgia.engine.eval.{Evaluator => BaseEvaluator}

package object bitboards {
  type Evaluator = BaseEvaluator[BitboardInstance, Int, Int, Int]
}
