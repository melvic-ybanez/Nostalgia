package controllers

import engine.board.{Board, Piece}
import engine.movegen.{Location, Move}

/**
  * Created by melvic on 9/14/18.
  */
trait BoardAccessor {
  def apply(location: Location): Option[Piece]
  def apply(row: Int, col: Int): Option[Piece]
  def moveBoard(move: Move[Location])
  def board: Board
}
