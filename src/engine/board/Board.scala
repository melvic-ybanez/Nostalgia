package engine.board

import engine.board.bitboards.Bitboard
import engine.movegen.{File, Location, Move, Rank}
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
  def at(file: File, rank: Rank): Option[Piece] = at(Location(file, rank))

  def updateByMove(move: Move[Location], piece: Piece): Board

  def apply(location: Location): Option[Piece] = at(location)
}


