package engine.movegen

/**
  * Created by melvic on 8/5/18.
  */
sealed trait MoveType

case object Normal extends MoveType
case object Attack extends MoveType
case object DoublePawnPush extends MoveType
case object EnPassant extends MoveType


case class Move[A](source: A, destination: A, moveType: MoveType = Normal)

object Move {
  type BitboardMove = Move[Int]
  type LocationMove = Move[Location]

  def transform[A](f: A => A): Move[A] => Move[A] = {
    case Move(source, destination, moveType) => Move(f(source), f(destination), moveType)
  }
}



