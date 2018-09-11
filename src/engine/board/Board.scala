package engine.board

import engine.movegen.{BitboardMove, Location, Move}

/**
  * Created by melvic on 8/6/18.
  */
object Board {
  val Size = 8
}

trait Board {
  def at(location: Location): Option[Piece]

  def updateByMove(move: Move[Location], piece: Piece): Board

  def apply(location: Location): Option[Piece] = at(location)
}


