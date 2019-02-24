package controllers

import engine.board._
import engine.movegen.Move.LocationMove
import models._
import validators.MoveValidator._
import views.boards.{BoardView, DefaultBoardView, HistoryView}

/**
  * Created by melvic on 9/11/18.
  */
trait BoardController {
  def sideToMove: Side

  def boardAccessor: BoardAccessor
  def validateMove: MoveValidation

  def menuController: MenuController
  def gameController: GameController

  def boardView: BoardView
  def historyView: HistoryView

  def gameType: GameType

  def newGame(lowerSide: Side, gameType: GameType): Unit
  def humanMove(move: LocationMove): Boolean
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
  override def boardAccessor = _boardAccessor
  override def gameType = _gameType

  def sideToMove_=(sideToMove: Side): Unit = _sideToMove = sideToMove
  def boardAccessor_=(boardAccessor: BoardAccessor): Unit = _boardAccessor = boardAccessor
  def gameType_=(gameType: GameType): Unit = _gameType = gameType

  override val boardView = DefaultBoardView(this)
  override val historyView = new HistoryView
  override val gameController = DefaultGameController(this)

  override def rotate(): Unit = {
    boardAccessor = boardAccessor match {
      case SimpleBoardAccessor(b) => RotatedBoardAccessor(b)
      case RotatedBoardAccessor(b) => SimpleBoardAccessor(b)
    }
    boardView.resetBoard()
  }

  override def newGame(lowerSide: Side, gameType: GameType): Unit = {
    gameController.stop()
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
    historyView.getItems.clear()

    val gameState = gameType match {
      case HumanVsHuman => HumanToMove
      case HumanVsComputer(White, _) => HumanToMove
      case _ => ComputerToMove
    }

    this.gameType = gameType
    gameController.gameState = gameState
    gameController.play()
  }

  override def humanMove(move: LocationMove): Boolean = {
    val netMove = boardAccessor.accessorMove(move)
    validateMove(netMove)(boardAccessor.board).exists { moveType =>
      boardAccessor.moveBoard(move.withType(moveType)).exists {
        case (accessor, piece, checkmate) =>
          handleMoveResult(move, netMove.withType(moveType), piece, accessor, checkmate)
          true
      }
    }
  }

  override def computerMove(): Unit = gameType match {
    case HumanVsComputer(_, level) =>
      val movedBoard = boardAccessor.board.updateByNextMove(sideToMove, level)
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
    boardView.resetBoard(false)
    sideToMove = sideToMove.opposite
    gameController.gameState = PreAnimation
  }
}
