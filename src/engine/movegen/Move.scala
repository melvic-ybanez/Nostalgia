package engine.movegen

import engine.board.Piece
import engine.movegen.Location._
import engine.movegen.Move.LocationMove

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
  def updatedType(newType: MoveType) = Move(source, destination, newType)
}

object Move {
  type BitboardMove = Move[Int]
  type LocationMove = Move[Location]

  def transform[A, B](f: A => B): Move[A] => Move[B] = {
    case Move(source, destination, moveType) => Move(f(source), f(destination), moveType)
  }
}



