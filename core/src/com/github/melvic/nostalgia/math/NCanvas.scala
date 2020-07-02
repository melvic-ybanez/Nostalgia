package com.github.melvic.nostalgia.math

import com.github.melvic.nostalgia.math.Coordinate.{GridCoordinate, CanvasCoordinate}

trait NCanvas[C] {
  def squareSize: Int

  def bounds: Bounds

  def offsettedCoordinate(
      coordinate: CanvasCoordinate, offset: Bounds => Double
  ): coordinate.Type = coordinate.value + offset(bounds)

  def scaledCoordinate(coordinate: GridCoordinate, f: CanvasCoordinate => Double): Double =
    f(CanvasCoordinate(coordinate.value * squareSize))

  def x(cX: CanvasCoordinate): cX.Type = offsettedCoordinate(cX, _.west)
  def y(cY: CanvasCoordinate): cY.Type = offsettedCoordinate(cY, _.north)

  def x(gX: GridCoordinate): Double = scaledCoordinate(gX, x)
  def y(gY: GridCoordinate): Double = scaledCoordinate(gY, y)

  def col(x: Double): Int = ((x - bounds.west) / squareSize).toInt
  def row(y: Double): Int = ((y - bounds.east) / squareSize).toInt
}

object NCanvas {
  def apply[C](implicit canvas: NCanvas[C]): NCanvas[C] = canvas
}
