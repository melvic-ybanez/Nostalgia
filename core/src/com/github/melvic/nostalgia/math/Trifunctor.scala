package com.github.melvic.nostalgia.math

import scala.language.higherKinds

trait Trifunctor[F[_, _, _]] {
  def trimap[A, B, C, D, E, G](fa: F[A, B, C])(
      f: A => D, g: B => E, h: C => G
  ): F[D, E, G]
}

object Trifunctor {
  def apply[F[_, _, _]](implicit trifunctor: Trifunctor[F]): Trifunctor[F] =
    trifunctor
}