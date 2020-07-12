package com.github.melvic.nostalgia.engine.board.bitboards

object PieceType {
  type PieceType = Int

  trait constants {
    val Pawn = 0
    val Knight = 1
    val Bishop = 2
    val Rook = 3
    val Queen = 4
    val King = 5
  }

  lazy val all = List(Pawn, Knight, Bishop, Rook, Queen, King)
}
