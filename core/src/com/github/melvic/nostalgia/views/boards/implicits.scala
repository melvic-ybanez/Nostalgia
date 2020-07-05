package com.github.melvic.nostalgia.views.boards

import com.github.melvic.nostalgia.math.{NCanvas, Padding, Point}

trait implicits {
  implicit val viewCanvas: NCanvas[Point] = new NCanvas[Point] {
    override def padding = Padding(40, 0, 0, 0)

    override def squareSize = 77

    override def x(canvas: Point) = canvas.x

    override def y(canvas: Point) = canvas.y

    override def init(x: Double, y: Double) = Point(x, y)
  }
}

object implicits extends implicits
