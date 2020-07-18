package com.github.melvic.nostalgia.engine.api.movegen

import com.github.melvic.nostalgia.engine.api.piece.Piece
import com.github.melvic.nostalgia.engine.base.MoveType
import com.github.melvic.nostalgia.engine.base.MoveType.Normal

final case class Move(from: Coordinate, to: Coordinate, moveType: MoveType[Piece])

/**
  * Created by melvic on 8/5/18.
  */
object Move {
  def normal(from: Coordinate, to: Coordinate) = Move(from, to, Normal)
  /**
    * Makes the coordinates compatible with the board view's
    * because the direction of the ranks are reversed
    */
  def locateMove(move: Move) = {
    val source = Coordinate.locateForView(move.from.rank, move.to.file)
    val dest = Coordinate.locateForView(move.destination.rank, move.destination.file)
    (source, dest)
  }
}



