package com.github.melvic.nostalgia.engine.base

import cats.Functor
import com.github.melvic.nostalgia.engine.base.MoveType.Normal

import scala.language.higherKinds

final case class Move[T, S, C](from: C, to: C, moveType: MoveType[Piece[T, S]])

object Move {
  def normal[T, S, C](from: C, to: C) = Move[T, S, C](from, to, Normal)

  implicit def moveFunctor[T, S]: Functor[Move[T, S, *]] = new Functor[Move[T, S, *]] {
    override def map[C, C1](move: Move[T, S, C])(f: C => C1) =
      move.copy(from = f(move.from), to = f(move.to))
  }
}