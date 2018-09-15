package controllers

import engine.board.{Side, White}
import engine.movegen.LocationMove
import validators.MoveValidator
import views.BoardView

/**
  * Created by melvic on 9/11/18.
  */
trait BoardController {
  def setSideToMove(sideToMove: Side)
  def sideToMove: Side
  def newGame(lowerSide: Side)
  def rotate(): Unit
  def boardAccessor: BoardAccessor
  def moveValidator: MoveValidator
  def boardView: BoardView
  def move(move: LocationMove): Unit
}

class DefaultBoardController extends BoardController {
  private var rotated: Boolean = false
  private var sideToMove: Side = White

  lazy val boardAccessor =
}
