package com.github.melvic.nostalgia.engine.board.bitboards

import com.github.melvic.nostalgia.engine.base.Square
import com.github.melvic.nostalgia.engine.board.Board

object Square {
  def fileOf(square: Int) = square % Board.Size

  def rankOf(square: Int) = square / Board.Size

  trait SquareImplicits {
    implicit val intSquare: Square[Int] = new Square[Int] {
      override type File = Int
      override type Rank = Int

      override def file(square: Int) = fileOf(square)

      override def rank(square: Int) = rankOf(square)
    }
  }
}
