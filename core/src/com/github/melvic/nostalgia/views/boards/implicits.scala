package com.github.melvic.nostalgia.views.boards

import com.github.melvic.nostalgia.math.{Bounds, NCanvas}
import javafx.scene.canvas.Canvas

trait implicits {
  implicit def defaultCanvas(implicit canvas: Canvas): NCanvas[Canvas] =
    new NCanvas[Canvas] {
      override def squareSize = 77

      override def offset = Bounds(40, 0, 0, 0)
    }
}

object implicits extends implicits
