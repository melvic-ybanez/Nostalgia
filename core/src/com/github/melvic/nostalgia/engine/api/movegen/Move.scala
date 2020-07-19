package com.github.melvic.nostalgia.engine.api.movegen

import cats.{Bifunctor, Functor}
import com.github.melvic.nostalgia.engine.api.piece.{Piece, PieceType, Side}
import com.github.melvic.nostalgia.engine.base.{Move => BaseMove, _}
import com.github.melvic.nostalgia.engine.board.bitboards._
import cats.implicits._

/**
  * Created by melvic on 8/5/18.
  */
object Move {
  type GenMove[C] = BaseMove[PieceType, Side, C]
  type Move = GenMove[Location]

  def normal(from: Location, to: Location): Move = BaseMove.normal(from, to)

  /**
    * Makes the coordinates compatible with the board view's
    * because the direction of the ranks are reversed
    */
  def locateMove(move: Move) = {
    val intMove = Functor[GenMove].map(move)(_.toBitPosition)

    val source = Location.locateForView(intMove.from.rank, intMove.from.file)
    val dest = Location.locateForView(intMove.to.rank, intMove.to.file)
    (source, dest)
  }
}



