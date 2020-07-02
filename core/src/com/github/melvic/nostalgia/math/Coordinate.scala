package com.github.melvic.nostalgia.math

import com.github.melvic.nostalgia.math

trait Coordinate[C] {
  type V

  def value: V
}

object Coordinate {
  final abstract class Board

  def apply[C, V1](value1: V1)(
      implicit aux: CoordinateType.Aux[C, V1]
  ): Coordinate[C] = new Coordinate[C] {
    override type V = V1

    override def value = value1
  }
}
