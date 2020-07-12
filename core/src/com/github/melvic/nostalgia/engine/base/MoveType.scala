package com.github.melvic.nostalgia.engine.base

sealed trait MoveType

object MoveType {
  case object Normal extends MoveType
  case object Attack extends MoveType
  case object DoublePawnPush extends MoveType
  case object EnPassant extends MoveType
  final case class PawnPromotion(newPosition: Piece) extends MoveType
  final case class Check(attacker: Piece) extends MoveType
  case object Castling extends MoveType
}