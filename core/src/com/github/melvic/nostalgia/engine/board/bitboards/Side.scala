package com.github.melvic.nostalgia.engine.board.bitboards
import com.github.melvic.nostalgia.engine.base.{Side => BaseSide}

object Side {
  type Side = Int

  trait constants {
    val White = 0
    val Black = 1
  }

  lazy val all = White :: Black :: Nil

  trait implicits {
    implicit class SideOps(side: Int) {
      def of(pieceType: Int) = Piece(pieceType, side)

      def opposite = if (side == White) Black else White
    }

    implicit val intSide: BaseSide[Int] = new BaseSide[Side] {
      override type T = Int

      override def of(side: Side, pieceType: Int) = side.of(pieceType)

      override def opposite(side: Side) = side.opposite
    }
  }
}
