package com.github.melvic.nostalgia.math

trait NCanvas[C] {
  def squareSize: Int

  def offset: Bounds

  def boardX(x: Double): Double = x + offset.west
  def boardY(y: Double): Double = y + offset.north

  def col(x: Double): Int = ((x - offset.west) / squareSize).toInt
  def row(y: Double): Int = ((y - offset.east) / squareSize).toInt
}

object NCanvas {
  def apply[C](implicit canvas: NCanvas[C]): NCanvas[C] = canvas
}
