package com.github.melvic.nostalgia.engine.movegen

import com.github.melvic.nostalgia.engine.base
import com.github.melvic.nostalgia.engine.board.Piece
import com.github.melvic.nostalgia.engine.movegen.Location._

/**
  * Created by melvic on 8/5/18.
  */
sealed trait MoveType

case object Normal extends base.MoveType
case object Attack extends base.MoveType
case object DoublePawnPush extends base.MoveType
case object EnPassant extends base.MoveType
case class PawnPromotion(newPosition: Piece) extends base.MoveType
case class Check(attacker: Piece) extends base.MoveType
case object Castling extends base.MoveType

case class Move[A](source: A, destination: A, moveType: base.MoveType = Normal) {
  def withType(newType: base.MoveType) = Move(source, destination, newType)
}

object Move {
  type BitboardMove = Move[Int]
  type LocationMove = Move[Location]

  def transform[A, B](f: A => B): Move[A] => Move[B] = {
    case Move(source, destination, moveType) => Move(f(source), f(destination), moveType)
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



