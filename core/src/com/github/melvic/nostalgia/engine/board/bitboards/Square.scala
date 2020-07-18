package com.github.melvic.nostalgia.engine.board.bitboards

import com.github.melvic.nostalgia.engine.base.{Board, Square => BaseSquare}

object Square {
  type Square = BaseSquare[Int, Int]

  def apply(file: Int, rank: Int): Square = BaseSquare(file, rank)

  def fileOf(square: Int) = square % Board.Size

  def rankOf(square: Int) = square / Board.Size

  trait SquareImplicits {
    implicit class SquareOps(square: Int) {
      def toSquare: Square = Square(fileOf(square), rankOf(square))
    }
  }
}
