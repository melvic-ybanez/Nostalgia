package com.github.melvic.nostalgia.engine.board

import com.github.melvic.nostalgia.engine.base

package object bitboards extends implicits
  with PieceType.constants
  with Side.all {

  type U64 = Bitboard.U64
  type Board = base.Board[Int, Int, Int]

  val BoardSize = base.Board.Size

  type Piece = Piece.Piece
  type Side = Side.Side
  type PieceType = PieceType.PieceType

  type Move = Move.Move
  type MoveType = Move.MoveType

  val types = PieceType.all
}
