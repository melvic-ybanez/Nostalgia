package controllers

import engine.board.{Board, Piece}
import engine.movegen.{Location, Move}
import engine.utils.Implicits.Locations._

/**
  * Created by melvic on 9/14/18.
  */
trait BoardAccessor {
  def apply(location: Location): Option[Piece] = board.at(accessorLocation(location))
  def apply(row: Int, col: Int): Option[Piece] = board.at(accessorLocation(Location(col, row)))
  def board: Board

  def accessorLocation: Location => Location = identity
  def accessorMove: Move[Location] => Move[Location] = Move.transform(accessorLocation)

  def moveBoard(move: Move[Location], piece: Piece): Unit = {
    board.updateByMove(accessorMove(move), piece)
  }
}

case class SimpleBoardAccessor(board: Board) extends BoardAccessor

case class RotatedBoardAccessor(board: Board) extends BoardAccessor {
  override def accessorLocation = location => Location(7 - location.file, 7 - location.rank)
}

