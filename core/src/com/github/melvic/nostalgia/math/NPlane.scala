package com.github.melvic.nostalgia.math

import com.github.melvic.nostalgia.math.NCell.{Col, Row}

trait NPlane[C] {
  def padding: Padding

  def cellSize: Double

  def x(instance: C): Double

  def y(instance: C): Double

  def init(x: Double, y: Double): C

  def initBounded(x: Double, y: Double): C =
    init(x + padding.left, y + padding.top)

  def boundedX(instance: C): Double = x(instance) - padding.left

  def boundedY(instance: C): Double = y(instance) - padding.top
}

object NPlane {
  def apply[C](implicit grid: NPlane[C]): NPlane[C] = grid
}

trait NPlaneImplicits {
  implicit class NPlaneOps[C: NPlane](instance: C) {
    def gridCoordinate(coordinate: Double): Int = (coordinate / instance.cellSize).toInt

    def toCell: NCell = NCell(
      Row(gridCoordinate(instance.boundedY)),
      Col(gridCoordinate(instance.boundedX))
    )

    def cellSize: Double = NPlane[C].cellSize

    def boundedY: Double = NPlane[C].boundedY(instance)

    def boundedX: Double = NPlane[C].boundedX(instance)
  }
}
