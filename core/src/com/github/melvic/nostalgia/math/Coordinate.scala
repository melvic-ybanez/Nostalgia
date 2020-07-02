package com.github.melvic.nostalgia.math

trait Coordinate {
  type Type

  def value: Type
}

object Coordinate {
  final case class GridCoordinate(value: Int) extends Coordinate {
    override type Type = Int
  }

  final case class CanvasCoordinate(value: Double) extends Coordinate {
    override type Type = Double
  }
}