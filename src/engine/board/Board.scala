package engine.board

import engine.board.bitboards.Bitboard
import engine.movegen._
import engine.movegen.Location._
import engine.movegen.Move.LocationMove

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

  def apply(location: Location): Option[Piece] = at(location)
  def apply(file: File, rank: Rank) = at(file, rank)

  def updateByMove(move: LocationMove, piece: Piece): Board

  def lastMove: Option[LocationMove]

  def locate(piece: Piece): List[Location]

  def isChecked(side: Side): Boolean
}


