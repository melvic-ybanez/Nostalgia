package com.github.melvic.nostalgia.engine.base

import cats.Bifunctor

trait Square[S] {
  type File
  type Rank

  def file(square: S): File

  def rank(square: S): Rank
}

object Square {
  def apply[S](implicit S: Square[S]): Square[S] = S
}
