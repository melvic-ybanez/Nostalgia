package com.github.melvic.nostalgia.math

import com.github.melvic.nostalgia.math.Coordinate.{GridCoordinate, CanvasCoordinate}

trait NCanvas[C] {
  type Offset = Bounds => Double

  def squareSize: Int

  def bounds: Bounds

  def offsettedCoordinate(
      coordinate: CanvasCoordinate, offset: Offset
  ): coordinate.Type = coordinate.value + offset(bounds)

  def scaledCoordinate(coordinate: GridCoordinate, f: CanvasCoordinate => Double): Double =
    f(CanvasCoordinate(coordinate.value * squareSize))

  def gridCoordinate(coordinate: CanvasCoordinate, offset: Offset) =
    ((coordinate.value - offset(bounds)) / squareSize).toInt

  def x(cX: CanvasCoordinate): cX.Type = offsettedCoordinate(cX, _.west)
  def y(cY: CanvasCoordinate): cY.Type = offsettedCoordinate(cY, _.north)

  def x(gX: GridCoordinate): Double = scaledCoordinate(gX, x)
  def y(gY: GridCoordinate): Double = scaledCoordinate(gY, y)

  def col(x: CanvasCoordinate): Int = gridCoordinate(x, _.west)
  def row(y: CanvasCoordinate): Int = gridCoordinate(y, _.east)
}

object NCanvas {
  def apply[C](implicit canvas: NCanvas[C]): NCanvas[C] = canvas
}
