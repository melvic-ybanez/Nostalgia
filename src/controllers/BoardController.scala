package controllers

import javafx.embed.swt.FXCanvas
import javafx.scene.control.Alert

import engine.board._
import engine.movegen.{G, Location, _5, _6}
import engine.movegen.Move.LocationMove
import models.{GameType, HumanVsComputer, HumanVsHuman}
import validators.MoveValidator._
import views.boards.{BoardView, DefaultBoardView, HistoryView}

/**
  * Created by melvic on 9/11/18.
  */
trait BoardController {
  def sideToMove: Side
  def gameType: GameType

  def boardAccessor: BoardAccessor
  def validateMove: MoveValidation

  def menuController: MenuController

  def boardView: BoardView
  def historyView: HistoryView

  def newGame(lowerSide: Side, gameType: GameType): Unit
  def move(move: LocationMove): Boolean
  def computerMove(): Unit
  def rotate(): Unit
}

case class DefaultBoardController(
    menuController: MenuController,
    initialBoard: Board,
    validateMove: MoveValidation) extends BoardController {
  private var _sideToMove: Side = White
  private var _gameType: GameType = HumanVsHuman
  private var _boardAccessor: BoardAccessor = SimpleBoardAccessor(initialBoard)

  override def sideToMove = _sideToMove
  override def gameType = _gameType
  override def boardAccessor = _boardAccessor

  def sideToMove_=(sideToMove: Side): Unit = _sideToMove = sideToMove
  def boardAccessor_=(boardAccessor: BoardAccessor): Unit = _boardAccessor = boardAccessor
  def gameType_=(gameType: GameType): Unit = _gameType = gameType

  override val boardView = DefaultBoardView(this)
  override val historyView = new HistoryView

  override def rotate(): Unit = {
    boardAccessor = boardAccessor match {
      case SimpleBoardAccessor(b) => RotatedBoardAccessor(b)
      case RotatedBoardAccessor(b) => SimpleBoardAccessor(b)
    }
    boardView.resetBoard()
  }

  override def newGame(lowerSide: Side, gameType: GameType): Unit = {
    sideToMove = White
    this.gameType = gameType
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
    historyView.getItems.clear()
  }

  override def move(move: LocationMove): Boolean = {
    val netMove = boardAccessor.accessorMove(move)
    validateMove(netMove)(boardAccessor.board).exists { moveType =>
      boardAccessor.moveBoard(move.updatedType(moveType)).exists {
        case (accessor, piece, checkmate) =>
          handleMoveResult(move, netMove.updatedType(moveType), piece, accessor, checkmate)
          if (!checkmate) computerMove()
          true
      }
    }
  }

  override def computerMove(): Unit = gameType match {
    case HumanVsComputer(humanSide) if humanSide != sideToMove =>
      val movedBoard = boardAccessor.board.updateByNextMove(sideToMove)
      val accessor = boardAccessor.updatedBoard(movedBoard)
      val move = movedBoard.lastMove.get
      val piece = movedBoard(move.destination).get
      handleMoveResult(move,
        accessor.accessorMove(move), piece, accessor, movedBoard.isCheckmate(sideToMove))
    case _ => ()
  }

  def handleMoveResult(
      move: LocationMove, accessorMove: LocationMove,
      piece: Piece,
      accessor: BoardAccessor,
      checkmate: Boolean): Unit = {
    historyView.addMove(accessorMove, boardAccessor.board, piece)
    boardAccessor = accessor
    boardView.resetBoard()
    boardView.highlight(move.destination)
    sideToMove = sideToMove.opposite
    if (checkmate) boardView.showCheckmateDialog(piece.side)
  }
}
