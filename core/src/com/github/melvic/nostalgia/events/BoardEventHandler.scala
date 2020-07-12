package com.github.melvic.nostalgia.events

import com.github.melvic.nostalgia.engine.base
import com.github.melvic.nostalgia.engine.board._
import com.github.melvic.nostalgia.engine.movegen._
import com.github.melvic.nostalgia.math.{NCoordinate, Point}
import com.github.melvic.nostalgia.views.boards._
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent

/**
  * Created by melvic on 9/16/18.
  */
trait BoardEventHandler extends EventHandler[MouseEvent] {
  override def handle(event: MouseEvent): Unit = {
    val controller = boardView.boardController

    def handleEvent(): Unit = {
      val cell = NCoordinate[Point].init(event.getX, event.getY).toCell

      val selectedLocation = Location.locateForView(cell.row, cell.col)
      val selectedPiece = controller.boardAccessor(selectedLocation)

      performAction(selectedPiece, selectedLocation)
    }

    if (boardView.boardController.humanToMove) handleEvent()
    else {
      reset()
      boardView.toggleHover(false)
    }
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

  def reset() = {
    this.sourcePiece = None
    this._sourceLocation = None
  }
}

case class PieceHoverEventHandler(boardView: BoardView) extends BoardEventHandler {
  override def performAction(selectedPiece: Option[Piece], selectedLocation: Location): Unit = boardView.toggleHover {
    val controller = boardView.boardController
    (sourcePiece, selectedPiece) match {
      case (None, Some(Piece(_, side))) if side == controller.sideToMove => true
      case (Some(Piece(_, sourceSide)), Some(Piece(_, selectedSide))) if sourceSide == selectedSide => true
      case (Some(_), _) =>
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
        if (side == boardController.sideToMove) updateSourceSquare()
        else ()

      // If the execution has made it this far, we can assume the source piece is present.
      case (_, _) if sourcePiece eq selectedPiece => undoSelection()
      case (Some(Piece(_, sourceSide)), Some(Piece(_, selectedSide))) if sourceSide == selectedSide =>
        updateSourceSquare()

      // handle potential pawn promotion (or proceed normally if there is none)
      case (Some(Piece(Pawn, sourceSide)), _) =>
        val accessorSelectedLocation = boardController.boardAccessor
          .accessorLocation(selectedLocation)
        sourceSide match {
          case White if accessorSelectedLocation.rank == _8 => promotePawn(White)
          case Black if accessorSelectedLocation.rank == _1 => promotePawn(Black)
          case _ => validateAndMove()
        }

      case _ => validateAndMove()
    }

    def validateAndMove(moveType: base.MoveType = Normal): Unit = {
      if (boardController.humanMove(Move[Location](sourceLocation, selectedLocation, moveType)))
        resetSourcePiece()
    }

    def promotePawn(side: Side): Unit = {
      val promotionDialog = PawnPromotionDialog(side)
      promotionDialog.showAndWait.ifPresent { _ =>
        val newPiece = Piece(promotionDialog.selectedPieceType, side)
        validateAndMove(PawnPromotion(newPiece))
      }
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

  def boardController = boardView.boardController
}