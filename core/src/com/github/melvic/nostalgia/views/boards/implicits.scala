package com.github.melvic.nostalgia.views.boards

import com.github.melvic.nostalgia.math.{NPlane, Padding, Point}

trait implicits {
  implicit val viewPlane: NPlane[Point] = new NPlane[Point] {
    override def padding = Padding(40, 0, 0, 0)

    override def cellSize = 77

    override def x(point: Point) = point.x

    override def y(point: Point) = point.y

    override def init(x: Double, y: Double) = Point(x, y)
  }
}

object implicits extends implicits
