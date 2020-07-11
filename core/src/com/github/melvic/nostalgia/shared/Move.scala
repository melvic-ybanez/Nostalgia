package com.github.melvic.nostalgia.shared

trait Move[M] {
  type R

  def from(move: M): M

  def to(move: M): M
}

object Move {
  def apply[M](implicit M: Move[M]): Move[M] = M
}