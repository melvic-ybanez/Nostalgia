package engine.board

import engine.movegen.{Location, Move}

/**
  * Created by melvic on 8/6/18.
  */
object Board {
  val Size = 8
}

trait Board {
  def at(location: Location): Option[Piece]
  def at(row: Int, col: Int): Option[Piece] = at(Location(row, col))

  def updateByMove(move: Move[Location], piece: Piece): Board

  def apply(location: Location): Option[Piece] = at(location)
}


