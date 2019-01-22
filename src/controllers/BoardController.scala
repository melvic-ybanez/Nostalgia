package controllers

import engine.board.{Black, Board, Side, White}
import engine.movegen.Move.LocationMove
import validators.MoveValidator._
import views.boards.{BoardView, DefaultBoardView, HistoryView}

/**
  * Created by melvic on 9/11/18.
  */
trait BoardController {
  def sideToMove: Side
  def boardAccessor: BoardAccessor
  def validateMove: MoveValidation

  def boardView: BoardView
  def historyView: HistoryView

  def newGame(lowerSide: Side): Unit
  def move(move: LocationMove): Boolean
  def rotate(): Unit
}

case class DefaultBoardController(initialBoard: Board, validateMove: MoveValidation) extends BoardController {
  private var _sideToMove: Side = White
  private var _boardAccessor: BoardAccessor = SimpleBoardAccessor(initialBoard)

  override def sideToMove = _sideToMove
  override def boardAccessor = _boardAccessor

  def sideToMove_=(sideToMove: Side): Unit = _sideToMove = sideToMove
  def boardAccessor_=(boardAccessor: BoardAccessor): Unit = _boardAccessor = boardAccessor

  override val boardView = DefaultBoardView(this)
  override val historyView = new HistoryView

  override def rotate(): Unit = {
    boardAccessor = boardAccessor match {
      case SimpleBoardAccessor(b) => RotatedBoardAccessor(b)
      case RotatedBoardAccessor(b) => SimpleBoardAccessor(b)
    }
    boardView.resetBoard()
  }

  override def newGame(lowerSide: Side): Unit = {
    sideToMove = White
    boardAccessor = boardAccessor.updatedBoard(initialBoard)
    lowerSide match {
      case Black => boardAccessor match {
        case SimpleBoardAccessor(_) => rotate()
        case _ => boardView.resetBoard()
      }
      case White => boardAccessor match {
        case RotatedBoardAccessor(_) => rotate()
        case _ => boardView.resetBoard()
      }
    }
    boardView.resetEventHandlers()
  }

  override def move(move: LocationMove): Boolean = {
    val netMove = boardAccessor.accessorMove(move)
    validateMove(netMove)(boardAccessor.board).exists { moveType =>
      boardAccessor.moveBoard(move.updatedType(moveType)).exists { case (accessor, piece) =>
        boardAccessor = accessor
        boardView.resetBoard()
        boardView.highlight(move.destination)
        historyView.addMove(piece, netMove)
        sideToMove = sideToMove.opposite
        true
      }
    }
  }
}
