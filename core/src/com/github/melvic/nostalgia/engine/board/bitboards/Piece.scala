package com.github.melvic.nostalgia.engine.board.bitboards

import com.github.melvic.nostalgia.engine.base.{Piece => BasePiece}

object Piece {
  type Piece = BasePiece[Int, Int]

  def apply(pieceType: Int, side: Int): Piece = BasePiece(pieceType, side)

  def unapply(piece: Piece): Option[(Int, Int)] = Some(piece.pieceType, piece.side)

  def white(pieceType: PieceType): Piece = Piece(pieceType, White)

  def black(pieceType: PieceType): Piece = Piece(pieceType, Black)
}
