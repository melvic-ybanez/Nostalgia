package com.github.melvic.nostalgia.engine.base

trait implicits extends Move.implicits with Square.LowPriorityImplicits

object implicits extends implicits {
  implicit class SquareOps[S: Square](instance: S) {
    def file: Square[S]#File = Square[S].file(instance)

    def rank: Square[S]#Rank = Square[S].rank(instance)
  }
}