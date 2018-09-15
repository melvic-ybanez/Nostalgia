package controllers

import engine.board.bitboards.Bitboard
import engine.board.{Black, Board, Side, White}
import engine.movegen.Move.LocationMove
import validators.MoveValidator
import views.boards.{BoardView, DefaultBoardView}

/**
  * Created by melvic on 9/11/18.
  */
trait BoardController {
  def sideToMove: Side
  def boardAccessor: BoardAccessor
  def moveValidator: MoveValidator
  def boardView: BoardView

  def setSideToMove(sideToMove: Side): Unit
  def newGame(lowerSide: Side): Unit
  def move(move: LocationMove): Unit
  def rotate(): Unit
}

case class DefaultBoardController(board: Board, moveValidator: MoveValidator) extends BoardController {
  private var side: Side = White
  private var accessor: BoardAccessor = SimpleBoardAccessor(board)

  override lazy val boardView = DefaultBoardView(this)

  override def setSideToMove(sideToMove: Side): Unit = this.side = sideToMove
  override def sideToMove = side
  override def boardAccessor = accessor

  override
  def rotate(): Unit = {
    accessor = accessor match {
      case SimpleBoardAccessor(b) => RotatedBoardAccessor(b)
      case RotatedBoardAccessor(b) => SimpleBoardAccessor(b)
    }
    boardView.resetBoard()
  }

  override
  def newGame(lowerSide: Side): Unit = {
    setSideToMove(lowerSide)
    accessor = accessor.updatedBoard(_ => board)
    lowerSide match {
      case Black => rotate()
      case _ => boardView.resetBoard()
    }
  }

  override
  def move(move: LocationMove): Unit = accessor.board(move.source).foreach { piece =>
    accessor.board.updateByMove(move, piece)
  }
}
