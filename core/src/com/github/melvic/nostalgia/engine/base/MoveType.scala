package com.github.melvic.nostalgia.engine.base

trait MoveType[+P]

object MoveType {
  case object Normal extends MoveType[Nothing]
  case object Attack extends MoveType[Nothing]
  case object DoublePawnPush extends MoveType[Nothing]
  case object EnPassant extends MoveType[Nothing]
  case object Castling extends MoveType[Nothing]

  final case class PawnPromotion[Piece](newPosition: Piece) extends MoveType[Piece]
  final case class Check[Piece](attacker: Piece) extends MoveType[Piece]
}
