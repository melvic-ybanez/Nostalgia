package com.github.melvic.nostalgia.engine.base

import cats.Bifunctor

final case class Square[F, R](file: F, rank: R)

object Square {
  implicit def squareBifunctor: Bifunctor[Square] = new Bifunctor[Square] {
    def bimap[F, R, F1, R1](square: Square[F, R])(f: F => F1, g: R => R1) =
      Square(f(square.file), g(square.rank))
  }
}
