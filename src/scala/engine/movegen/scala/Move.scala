package scala.engine.movegen.scala

/**
  * Created by melvic on 8/5/18.
  */
sealed trait MoveType

case object Normal extends MoveType
case object Attack extends MoveType
case object DoublePawnPush extends MoveType
case object EnPassant extends MoveType

trait Move[A] {
  def source: A
  def destination: A
  def moveType: MoveType
}

/**
  * This is a frontend-friendly Move sub-class. Use this only when you
  * plan to integrate the scala.engine with the GUI.
  */
case class LocationMove(source: Location, destination: Location, moveType: MoveType) extends Move[Location]

case class BitboardMove(source: Int, destination: Int, moveType: MoveType) extends Move[Int]



