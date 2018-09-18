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
  def performAction(selectedPiece: Option[Piece], selectedLocation: Location): Unit

  private var _sourcePiece: Option[Piece] = None
  def sourcePiece = _sourcePiece
  def sourcePiece_=(sourcePiece: Option[Piece]) = _sourcePiece = sourcePiece

  private var _sourceLocation: Option[Location] = None
  def sourceLocation = _sourceLocation.get
  def sourceLocation_=(sourceLocation: Location) = _sourceLocation = Some(sourceLocation)

  def updateSourceSquare(selectedPiece: Option[Piece], selectedLocation: Location): Unit = {
    sourcePiece = selectedPiece
    sourceLocation = selectedLocation
  }
}

case class PieceHoverEventHandler(boardView: BoardView) extends BoardEventHandler {
  override def performAction(selectedPiece: Option[Piece], selectedLocation: Location): Unit = boardView.toggleHover {
    (sourcePiece, selectedPiece) match {
      case (None, Some(Piece(_, side))) if side == boardView.boardController.sideToMove => true
      case (Some(Piece(_, sourceSide)), Some(Piece(_, selectedSide))) if sourceSide == selectedSide => true
      case (Some(_), _) =>
        val controller = boardView.boardController
        controller.validateMove(controller.boardAccessor.move(sourceLocation, selectedLocation)) {
          controller.boardAccessor.board
        }.isDefined
      case _ => false
    }
  }
}

case class MoveEventHandler(boardView: BoardView, hoverEventHandler: PieceHoverEventHandler) extends BoardEventHandler {
  override def performAction(selectedPiece: Option[Piece], selectedLocation: Location) = {
    (sourcePiece, selectedPiece) match {
      case (None, None) => ()
      case (None, Some(Piece(_, side))) =>
        if (side == boardView.boardController.sideToMove) updateSourceSquare()
        else ()

      // If the execution has made it this far, we can assume the source piece is present.
      case (_, _) if sourcePiece eq selectedPiece => undoSelection()
      case (Some(Piece(_, sourceSide)), Some(Piece(_, selectedSide))) if sourceSide == selectedSide =>
        updateSourceSquare()
      case _ =>
        if (boardView.boardController.move(Move[Location](sourceLocation, selectedLocation)))
          resetSourcePiece()
    }

    def updateSourceSquare(): Unit = {
      this.updateSourceSquare(selectedPiece, selectedLocation)
      hoverEventHandler.updateSourceSquare(selectedPiece, selectedLocation)

      boardView.highlight(selectedLocation)
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