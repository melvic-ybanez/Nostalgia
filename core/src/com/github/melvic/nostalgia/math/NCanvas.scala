package com.github.melvic.nostalgia.math

trait NCanvas[C] {
  def padding: Padding

  def squareSize: Double

  def x(canvas: C): Double

  def y(canvas: C): Double

  def init(x: Double, y: Double): C

  def initBounded(x: Double, y: Double): C =
    init(x + padding.left, y + padding.top)

  def boundedX(canvas: C): Double = x(canvas) - padding.left

  def boundedY(canvas: C): Double = y(canvas) - padding.top
}

object NCanvas {
  def apply[C](implicit canvas: NCanvas[C]): NCanvas[C] = canvas
}

trait NCanvasImplicits {
  implicit class NCanvasOps[C: NCanvas](canvas: C) {
    def gridCoordinate(coordinate: Double): Int = (coordinate / canvas.squareSize).toInt

    def toCell: NCell = NCell(
      gridCoordinate(canvas.boundedY),
      gridCoordinate(canvas.boundedX)
    )

    def squareSize: Double = NCanvas[C].squareSize

    def boundedY: Double = NCanvas[C].boundedY(canvas)

    def boundedX: Double = NCanvas[C].boundedX(canvas)
  }
}
