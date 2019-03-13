package ybanez.nostalgia.engine.board

import ybanez.nostalgia.engine.board.bitboards.Bitboard
import ybanez.nostalgia.engine.movegen._
import ybanez.nostalgia.engine.movegen.Location._
import ybanez.nostalgia.engine.movegen.Move.LocationMove

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

  def generateMoves(sideToMove: Side): Stream[(LocationMove, Piece)]

  def updateByNextMove(sideToMove: Side, depth: Int): Board

  def isChecked(side: Side): Boolean
  def isCheckmate(winningSide: Side): Boolean

  /**
    * Checks if the king, given its move, can still castle. That is, if the king
    * and the rook has not moved yet.
    * Note that this method does not check if a castle is possible with the current
    * board position. For instance, if the path to the castling destination is blocked or
    * is being threaten by an opponent piece, this method might still return true if the
    * conditions above are met.
    * @param kingMove Location of the king
    * @return Whether the king can still castle.
    */
  def canCastle(kingMove: LocationMove): Boolean

  def pieceLocations(piece: Piece): Stream[Location]
}


