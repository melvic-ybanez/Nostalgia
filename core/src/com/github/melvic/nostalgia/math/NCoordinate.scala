package com.github.melvic.nostalgia.math

trait NCoordinate[C] {
  def offsets: NPadding

  def size: Double

  def x(instance: C): Double

  def y(instance: C): Double

  def init(x: Double, y: Double): C

  def initBounded(x: Double, y: Double): C =
    init(x + offsets.left, y + offsets.top)

  def boundedX(instance: C): Double = x(instance) - offsets.left

  def boundedY(instance: C): Double = y(instance) - offsets.top
}

object NCoordinate {
  def apply[C](implicit grid: NCoordinate[C]): NCoordinate[C] = grid
}

trait NPlaneImplicits {
  implicit class NPlaneOps[C: NCoordinate](instance: C) {
    def gridCoordinate(coordinate: Double): Int = (coordinate / instance.size).toInt

    def toCell: NCell = NCell(
      gridCoordinate(instance.boundedX),
      gridCoordinate(instance.boundedY)
    )

    def size: Double = NCoordinate[C].size

    def boundedY: Double = NCoordinate[C].boundedY(instance)

    def boundedX: Double = NCoordinate[C].boundedX(instance)
  }
}
