package com.github.melvic.nostalgia.views.boards

import com.github.melvic.nostalgia.math.{NCoordinate, NPadding, Point}

trait implicits {
  implicit val viewPlane: NCoordinate[Point] = new NCoordinate[Point] {
    override def offsets = NPadding(40, 0, 0, 0)

    override def size = 77

    override def x(point: Point) = point.x

    override def y(point: Point) = point.y

    override def init(x: Double, y: Double) = Point(x, y)
  }
}

object implicits extends implicits
