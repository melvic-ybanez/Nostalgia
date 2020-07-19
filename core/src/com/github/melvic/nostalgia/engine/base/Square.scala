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

  trait lowPriorityImplicits {
    implicit class SquareAuxOps[S, F, R](instance: S)(implicit square: Aux[S, F, R]) {
      def file: F = square.file(instance)

      def rank: R = square.rank(instance)
    }
  }
}
