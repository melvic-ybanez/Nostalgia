package com.github.melvic.nostalgia.engine.base

import com.github.melvic.nostalgia.engine.base.Square.Aux

trait implicits extends Square.LowPriorityImplicits

/**
 * Contains the high-priority implicits
 */
object implicits extends implicits {
  implicit class SquareAuxOps[S, F, R](instance: S)(implicit square: Aux[S, F, R]) {
    def file: F = square.file(instance)

    def rank: R = square.rank(instance)
  }
}