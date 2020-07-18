package com.github.melvic.nostalgia.engine.board

import com.github.melvic.nostalgia.engine.base

package object bitboards extends implicits
  with PieceType.constants
  with Side.all {

  type U64 = Bitboard.U64
  type Board = base.Board[Bitboard, Int, Int, Int]

  def Board(implicit board: Board): Board = board

  type Piece = Piece.Piece
  type Side = Side.Side
  type PieceType = PieceType.PieceType

  type Square = Square.Square

  type Move = Move.Move
  type MoveType = Move.MoveType

  val types = PieceType.all
}
