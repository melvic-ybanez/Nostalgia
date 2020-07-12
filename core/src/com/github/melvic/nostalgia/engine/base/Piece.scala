package com.github.melvic.nostalgia.engine.base

import cats.Bifunctor

final case class Piece[T, S](pieceType: T, side: S)

object Piece {
  implicit val pieceBiFunctor: Bifunctor[Piece] = new Bifunctor[Piece] {
    override def bimap[A, B, C, D](piece: Piece[A, B])(f: A => C, g: B => D) =
      Piece(f(piece.pieceType), g(piece.side))
  }
}