package com.github.melvic.nostalgia.engine.api.piece

sealed trait PieceType

object PieceType {
  case object Pawn extends PieceType
  case object Knight extends PieceType
  case object Bishop extends PieceType
  case object Rook extends PieceType
  case object Queen extends PieceType
  case object King extends PieceType

  lazy val all = List(Pawn, Knight, Bishop, Rook, Queen, King)
}
