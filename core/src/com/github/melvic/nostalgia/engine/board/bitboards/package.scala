package com.github.melvic.nostalgia.engine.board

import com.github.melvic.nostalgia.engine.base.Move
import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard.Bitboard

package object bitboards extends implicits
  with PieceType.constants
  with Side.all {

  type BBBoard = Board[Bitboard, Int, Int, Int]

  type U64 = Bitboard.U64

  type Piece = Piece.Piece
  type Side = Side.Side
  type PieceType = PieceType.PieceType

  type Move = Move.Move
  type MoveType = Move.MoveType

  val types = PieceType.all
}
