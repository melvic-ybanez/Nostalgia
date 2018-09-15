package engine.board

import engine.board.bitboards.Bitboard
import engine.movegen.{Location, Move}
import engine.movegen.Location._

/**
  * Created by melvic on 8/6/18.
  */
object Board {
  val Size = 8
  lazy val defaultBoard = Bitboard()
}

trait Board {
  def at(location: Location): Option[Piece]
  def at(row: Int, col: Int): Option[Piece] = at(Location(col, row))

  def updateByMove(move: Move[Location], piece: Piece): Board

  def apply(location: Location): Option[Piece] = at(location)
}


