package main

import engine.board.Piece

/**
  * Created by melvic on 9/15/18.
  */
object Resources {
  def pathOf(name: String): String =
    Resources.getClass.getResource("resources/images/" + name).toString

  def piecePathOf: Piece => String = {
    case Piece(pieceType, side) =>
      // TODO: This is a hackish approach. Improve this later.
      val stringify = (x: Any) => x.toString.toLowerCase

      pathOf(s"pieces/${stringify(side)}_${stringify(pieceType)}.png")
  }
}
