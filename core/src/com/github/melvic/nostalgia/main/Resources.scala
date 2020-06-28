package com.github.melvic.nostalgia.main

import com.github.melvic.nostalgia.engine.board.{Black, Knight, Piece, Rook, Side, White}

/**
  * Created by melvic on 9/15/18.
  */
object Resources {
  def apply(name: String): String =
    Resources.getClass.getResource(s"resources/$name").toExternalForm

  def image(name: String): String = Resources(s"images/$name")

  def styleSheets(name: String): String = Resources(s"$name.css")

  def piecePathOf(piece: Piece, front: Boolean = true): String = piece match {
    case Piece(pieceType, side) =>
      def shortName(name: String) = name.substring(0, 1).toUpperCase

      val typeString = pieceType match {
        case Knight => "H"
        case _ => shortName(pieceType.toString)
      }
      val sideString = shortName(side.toString)
      val directionString = if (front) "F" else "B"
      val filename = s"$typeString$sideString$directionString.png"

      image(s"pieces/$filename")
  }
}
