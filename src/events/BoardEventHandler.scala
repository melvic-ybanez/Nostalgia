package events

import javafx.event.EventHandler
import javafx.scene.input.MouseEvent

import engine.board.Piece
import engine.movegen.{Location, Move}
import views.boards.BoardView

/**
  * Created by melvic on 9/16/18.
  */
trait BoardEventHandler extends EventHandler[MouseEvent] {
  override def handle(event: MouseEvent): Unit = {
    val col = (event.getX / boardView.squareSize).toInt
    val row = (event.getY / boardView.squareSize).toInt

    val selectedLocation = Location.locate(row, col)
    val selectedPiece = boardView.boardController.boardAccessor(selectedLocation)

    performAction(selectedPiece, selectedLocation)
  }

  def boardView: BoardView
  def performAction(selectedPiece: Option[Piece], selectedLocation: Location)

  private var _sourcePiece: Option[Piece] = None
  def sourcePiece = _sourcePiece
  def sourcePiece_=(sourcePiece: Option[Piece]) = _sourcePiece = sourcePiece
}

case class PieceHoverEventHandler(boardView: BoardView) extends BoardEventHandler {
  override def performAction(selectedPiece: Option[Piece], selectedLocation: Location): Unit = {
    boardView.toggleHover(sourcePiece.isDefined || selectedPiece.map { case Piece(_, side) =>
      side == boardView.boardController.sideToMove
    }.get)
  }
}

case class MoveEventHandler(boardView: BoardView, hoverEventHandler: PieceHoverEventHandler) extends BoardEventHandler {
  private var _sourceLocation: Option[Location] = None

  def sourceLocation = _sourceLocation.get
  def sourceLocation_=(sourceLocation: Location) = _sourceLocation = Some(sourceLocation)

  override def performAction(selectedPiece: Option[Piece], selectedLocation: Location) = {
    (sourcePiece, selectedPiece) match {
      case (None, None) => ()
      case (None, Some(Piece(_, side))) =>
        if (side == boardView.boardController.sideToMove) {
          replaceSourceSquare(selectedPiece, selectedLocation)
          boardView.highlight(selectedLocation)
        } else ()

      // If the execution has made it this far, we can assume the source piece is present.
      case (Some(Piece(_, sourceSide)), _) =>
        boardView.highlight(selectedLocation)

        if (sourcePiece == selectedPiece) undoSelection()
        else selectedPiece match {
          case Some(Piece(_, selectedSide)) if sourceSide == selectedSide =>
            replaceSourceSquare(selectedPiece, selectedLocation)
          case _ =>
            boardView.boardController.move(Move[Location](sourceLocation, selectedLocation))
            resetSourcePiece()
        }
    }

    def replaceSourceSquare(sourcePiece: Option[Piece], sourceLocation: Location): Unit = {
      this.sourcePiece = sourcePiece
      this.sourceLocation = sourceLocation
      hoverEventHandler.sourcePiece = sourcePiece
    }

    def resetSourcePiece(): Unit = {
      sourcePiece = None
      hoverEventHandler.sourcePiece = None
    }

    def undoSelection(): Unit = {
      boardView.resetBoard()
      resetSourcePiece()
    }
  }
}