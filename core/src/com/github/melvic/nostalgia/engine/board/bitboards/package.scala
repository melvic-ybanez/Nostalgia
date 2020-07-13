package com.github.melvic.nostalgia.engine.board

import com.github.melvic.nostalgia.engine.base.Move

package object bitboards extends implicits
  with PieceType.constants
  with Side.all {

  type U64 = Bitboard.U64
  type Bitboard = Board[BitboardInstance, Int, Int, Int]

  type Piece = Piece.Piece
  type Side = Side.Side
  type PieceType = PieceType.PieceType

  type Move = Move.Move
  type MoveType = Move.MoveType

  val types = PieceType.all
}
