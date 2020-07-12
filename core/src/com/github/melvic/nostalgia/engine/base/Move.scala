package com.github.melvic.nostalgia.engine.base

import com.github.melvic.nostalgia.engine.base.MoveType.Normal

import scala.language.higherKinds

final case class Move[T, S: Side, L](from: L, to: L, moveType: MoveType[Piece[T, S]])

object Move {
  def normal[T, S: Side, L](from: L, to: L) = Move[T, S, L](from, to, Normal)
}