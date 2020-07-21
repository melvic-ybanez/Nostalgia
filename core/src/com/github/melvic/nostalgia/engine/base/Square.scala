package com.github.melvic.nostalgia.engine.base

import scala.language.higherKinds

trait Square[S] {
  type File
  type Rank

  def file(square: S): File

  def rank(square: S): Rank
}

object Square {
  type Aux[S, F, R] = Square[S] {
    type File = F
    type Rank = R
  }

  def apply[S](implicit square: Square[S]): Square[S] = square

  trait LowPriorityImplicits {
    implicit class SquareOps[S: Square](instance: S) {
      def file: Square[S]#File = Square[S].file(instance)

      def rank: Square[S]#Rank = Square[S].rank(instance)
    }
  }
}
