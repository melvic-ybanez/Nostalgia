package com.github.melvic.nostalgia.engine.base

import cats.{Bifunctor, Functor}
import com.github.melvic.nostalgia.engine.base.MoveType.Normal
import com.github.melvic.nostalgia.math.Trifunctor

import scala.language.higherKinds

final case class Move[T, S, C](from: C, to: C, moveType: MoveType[T, S])

object Move {
  def normal[T, S, C](from: C, to: C) = Move[T, S, C](from, to, Normal)

  implicit def moveFunctor[T, S]: Functor[Move[T, S, *]] = new Functor[Move[T, S, *]] {
    override def map[C, C1](move: Move[T, S, C])(f: C => C1) =
      move.copy(from = f(move.from), to = f(move.to))
  }

  implicit def moveTrifunctor(
      implicit moveTypeBifunctor: Bifunctor[MoveType]
  ): Trifunctor[Move] = new Trifunctor[Move] {
    override def trimap[A, B, C, D, E, G](move: Move[A, B, C])(
        f: A => D, g: B => E, h: C => G
    ): Move[D, E, G] = move.copy(
      from = h(move.from),
      to = h(move.to),
      moveType = Bifunctor[MoveType].bimap(move.moveType)(f, g)
    )
  }
}