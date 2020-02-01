package com.github.melvic.nostalgia.main

import com.github.melvic.nostalgia.engine.board.Piece

/**
  * Created by melvic on 9/15/18.
  */
object Resources {
  def apply(name: String): String =
    Resources.getClass.getResource(s"resources/$name").toExternalForm

  def image(name: String): String = Resources(s"images/$name")

  def styleSheets(name: String): String = Resources(s"$name.css")

  def piecePathOf: Piece => String = {
    case Piece(pieceType, side) =>
      // TODO: This is a hackish approach. Improve this later.
      val stringify = (x: Any) => x.toString.toLowerCase

      image(s"pieces/${stringify(side)}_${stringify(pieceType)}.png")
  }
}
