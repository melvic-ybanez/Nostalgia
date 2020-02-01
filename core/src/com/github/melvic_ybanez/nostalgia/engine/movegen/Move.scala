package com.github.melvic_ybanez.nostalgia.engine.movegen

import com.github.melvic_ybanez.nostalgia.engine.board.Piece
import com.github.melvic_ybanez.nostalgia.engine.movegen.Location._

/**
  * Created by melvic on 8/5/18.
  */
sealed trait MoveType

case object Normal extends MoveType
case object Attack extends MoveType
case object DoublePawnPush extends MoveType
case object EnPassant extends MoveType
case class PawnPromotion(newPosition: Piece) extends MoveType
case class Check(attacker: Piece) extends MoveType
case object Castling extends MoveType

case class Move[A](source: A, destination: A, moveType: MoveType = Normal) {
  def withType(newType: MoveType) = Move(source, destination, newType)
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



