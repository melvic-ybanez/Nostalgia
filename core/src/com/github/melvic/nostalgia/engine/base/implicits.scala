package com.github.melvic.nostalgia.engine.base

import cats.Functor

object implicits {
  implicit val pieceFunctor: Functor[Piece] = new Functor[Piece] {
    override def map[A, B](piece: Piece[A])(f: A => B) =
      Piece(f(piece.pieceType), f(piece.side))
  }
}
