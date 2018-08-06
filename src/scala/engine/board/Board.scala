package scala.engine.board

import java.engine.board.Piece

import scala.engine.movegen.scala.{BitboardMove, Location, Move}

/**
  * Created by melvic on 8/6/18.
  */
object Board {
  val Size = 8
}

trait Board {
  def at(location: Location): Option[Piece]

  def updateByMove(move: Move[Location], piece: Piece): Board
}


