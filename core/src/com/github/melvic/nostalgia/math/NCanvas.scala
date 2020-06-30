package com.github.melvic.nostalgia.math

trait NCanvas[C] {
  def squareSize: Int

  def offset: Bounds
}

object NCanvas {
  def apply[C](implicit canvas: NCanvas[C]): NCanvas[C] = canvas

  implicit val defaultCanvas: NCanvas[Default] = new NCanvas[Default] {
    override def squareSize = 77

    override def offset: Bounds = Bounds(40, 0, 0, 0)
  }
}
