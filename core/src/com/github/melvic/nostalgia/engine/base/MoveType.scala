package com.github.melvic.nostalgia.engine.base

import cats.Bifunctor

trait MoveType[+T, +S]

object MoveType {
  type MoveType0 = MoveType[Nothing, Nothing]

  case object Normal extends MoveType0
  case object Attack extends MoveType0
  case object DoublePawnPush extends MoveType0
  case object EnPassant extends MoveType0
  case object Castling extends MoveType0

  final case class PawnPromotion[T, S](newRank: Piece[T, S]) extends MoveType[T, S]
  final case class Check[T, S](attacker: Piece[T, S]) extends MoveType[T, S]

  implicit def moveTypeBifunctor(
      implicit pieceBifunctor: Bifunctor[Piece]
  ): Bifunctor[MoveType] = new Bifunctor[MoveType] {
    override def bimap[A, B, C, D](fab: MoveType[A, B])(f: A => C, g: B => D) = fab match {
      case PawnPromotion(newRank) => PawnPromotion(Bifunctor[Piece].bimap(newRank)(f, g))
      case Check(attacker) => Check(Bifunctor[Piece].bimap(attacker)(f, g))
      case other: MoveType0 => other
    }
  }
}
