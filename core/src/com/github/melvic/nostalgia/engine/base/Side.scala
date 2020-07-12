package com.github.melvic.nostalgia.engine.base

trait Side[S] {
  type T

  def of(side: S, pieceType: T): Piece[T, S]

  def opposite(side: S): S
}

object Side {
  def apply[S](implicit S: Side[S]) = S
}
