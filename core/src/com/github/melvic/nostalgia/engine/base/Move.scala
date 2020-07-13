package com.github.melvic.nostalgia.engine.base

import cats.Functor
import com.github.melvic.nostalgia.engine.base.MoveType.Normal

import scala.language.higherKinds

final case class Move[T, S: Side, L](from: L, to: L, moveType: MoveType[Piece[T, S]])

object Move {
  def normal[T, S: Side, L](from: L, to: L) = Move[T, S, L](from, to, Normal)

  implicit def moveFunctor[T, S: Side]: Functor[Move[T, S, *]] = new Functor[Move[T, S, *]] {
    override def map[A, B](move: Move[T, S, A])(f: A => B) =
      move.copy(from = f(move.from), to = f(move.to))
  }
}