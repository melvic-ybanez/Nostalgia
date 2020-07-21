package com.github.melvic.nostalgia.engine.board.bitboards

import com.github.melvic.nostalgia.engine.base.{
  MoveType => BaseMoveType,
  Move => BaseMove,
  Piece => BasePiece
}

object Move {
  type Move = BaseMove[Int, Int, Int]
  type MoveType = BaseMoveType[Int, Int]

  def apply(from: Int, to: Int, moveType: MoveType) =
    BaseMove[Int, Int, Int](from, to, moveType)

  def normal(from: Int, to: Int): Move = BaseMove.normal(from, to)

  def unapply(move: Move): Option[(Int, Int, MoveType)] =
    Some(move.from, move.to, move.moveType)
}
