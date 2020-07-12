package com.github.melvic.nostalgia.engine.movegen

import com.github.melvic.nostalgia.engine.base
import com.github.melvic.nostalgia.engine.board.Piece
import com.github.melvic.nostalgia.engine.movegen.Location._

/**
  * Created by melvic on 8/5/18.
  */

case class MMove[A](source: A, destination: A, moveType: MoveType = Normal) {
  def withType(newType: MoveType) = MMove(source, destination, newType)
}

object MMove {
  type BitboardMove = MMove[Int]
  type LocationMove = MMove[Location]

  def transform[A, B](f: A => B): MMove[A] => MMove[B] = {
    case MMove(source, destination, moveType) => Move(f(source), f(destination), moveType)
  }

  /**
    * Makes the coordinates compatible with the board view's
    * because the direction of the ranks are reversed
    */
  def locateMove(move: LocationMove) = {
    val source = Location.locateForView(move.source.rank, move.source.file)
    val dest = Location.locateForView(move.destination.rank, move.destination.file)
    (source, dest)
  }
}



