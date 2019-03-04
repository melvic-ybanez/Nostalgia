package controllers

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.{FXCollections, ObservableList}
import javafx.concurrent.Task

import engine.board._
import engine.movegen.Move.LocationMove
import models._
import validators.MoveValidator._
import views.boards.{BoardView, DefaultBoardView, HistoryView}

/**
  * Created by melvic on 9/11/18.
  */
trait GameController {
  def sideToMove: Side

  def boardAccessor: BoardAccessor
  def validateMove: MoveValidation

  def menuController: MenuController

  def boardView: BoardView
  def historyView: HistoryView

  def gameType: GameType

  def newGame(lowerSide: Side, gameType: GameType): Unit
  def humanMove(move: LocationMove): Boolean
  def computerMove(): Unit
  def rotate(): Unit
  def undo(): Unit
  def redo(): Unit

  def gameOver(winningSide: Side, reason: String): Unit

  def humanToMove = gameType match {
    case HumanVsHuman => true
    case HumanVsComputer(humanSide, _) if sideToMove == humanSide => true
    case _ => false
  }

  final def computerToMove = !humanToMove

  val gameOnGoingProperty: SimpleBooleanProperty = new SimpleBooleanProperty()

  val historyBoards: ObservableList[(Board, Side)] = FXCollections.observableArrayList()
  val undoneBoards: ObservableList[(Board, Side)] = FXCollections.observableArrayList()
}

case class DefaultGameController(
    menuController: MenuController,
    initialBoard: Board,
    validateMove: MoveValidation) extends GameController {
  private var _sideToMove: Side = Preferences.Defaults.sideToPlay
  private var _gameType: GameType = Preferences.Defaults.gameType
  private var _boardAccessor: BoardAccessor = SimpleBoardAccessor(initialBoard)

  override def sideToMove = _sideToMove
  override def boardAccessor = _boardAccessor
  override def gameType = _gameType

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
    historyView.getItems.clear()

    Platform.runLater { () =>
      boardView.resetEventHandlers()
      gameOnGoingProperty.set(true)
      historyBoards.clear()
      undoneBoards.clear()
    }

    this.gameType = gameType
    if (computerToMove) computerMove()
  }

  override def humanMove(move: LocationMove): Boolean = {
    val netMove = boardAccessor.accessorMove(move)
    validateMove(netMove)(boardAccessor.board).exists { moveType =>
      historyBoards.add((boardAccessor.board, sideToMove))
      boardAccessor.moveBoard(move.withType(moveType)).exists {
        case (accessor, piece, checkmate) =>
          handleMoveResult(move, netMove.withType(moveType), piece, accessor, checkmate)
          true
      }
    }
  }

  override def computerMove(): Unit = gameType match {
    case HumanVsComputer(_, level) =>
      val task = new Task[Board]() {
        override def call(): Board = boardAccessor.board.updateByNextMove(sideToMove, level)
      }

      task.setOnSucceeded { _ =>
        val movedBoard = task.getValue
        val accessor = boardAccessor.updatedBoard(movedBoard)
        val move = movedBoard.lastMove.get
        val piece = movedBoard(move.destination).get
        handleMoveResult(move,
          accessor.accessorMove(move), piece, accessor, movedBoard.isCheckmate(sideToMove))
      }

      val thread = new Thread(task)
      thread.setDaemon(true)
      thread.start()
    case _ => ()
  }

  override def gameOver(winningSide: Side, reason: String) = {
    boardView.showGameOverDialog(winningSide, reason)
    boardView.removeListeners()
    gameOnGoingProperty.set(false)
  }

  def handleMoveResult(
      move: LocationMove, accessorMove: LocationMove,
      piece: Piece,
      accessor: BoardAccessor,
      checkmate: Boolean): Unit = {
    historyView.addMove(accessorMove, boardAccessor.board, piece)
    boardAccessor = accessor
    boardView.resetBoard(false)
    boardView.animateMove {
      if (checkmate) gameOver(sideToMove, "checkmate")
      undoneBoards.clear()
      if (computerToMove) computerMove()
    }
    sideToMove = sideToMove.opposite
  }

  override def undo() = _undo(historyBoards, undoneBoards)
  override def redo() = _undo(undoneBoards, historyBoards)

  private def _undo(historyBoards: ObservableList[(Board, Side)],
      undoneBoards: ObservableList[(Board, Side)]) = {
    undoneBoards.add((boardAccessor.board, sideToMove))
    val (lastBoard, lastSideToMove) = historyBoards.remove(historyBoards.size - 1)
    boardAccessor = boardAccessor.updatedBoard(lastBoard)
    boardView.resetBoard()
    sideToMove = lastSideToMove
  }
}
