package com.github.melvic.nostalgia.engine.board.bitboards

import com.github.melvic.nostalgia.engine.base.{Board, Square}

trait SquareImplicits {
  implicit object IntSquare extends Square[Int] {
    override type File = Int
    override type Rank = Int

    override def file(square: Int): Int = square % Board.Size

    override def rank(square: Int): Int = square / Board.Size
  }
}
