package ybanez.nostalgia.views.boards

import javafx.scene.Cursor
import javafx.scene.control.{ButtonType, Dialog, Label}
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox

import ybanez.nostalgia.engine.board._
import ybanez.nostalgia.main.Resources

/**
  * Created by melvic on 9/18/18.
  */
case class PawnPromotionDialog(side: Side) extends Dialog[ButtonType] {
  private var _selectedPieceType: PieceType = Queen

  def selectedPieceType = _selectedPieceType
  def selectedPieceType_=(selectedPieceType: PieceType): Unit =
    _selectedPieceType = selectedPieceType

  setTitle("Pawn Promotion")

  getDialogPane.setContent {
    val contentPane = new HBox

    val officers = Queen :: Rook :: Bishop :: Knight :: Nil
    val pieceButtons = officers.map { piece =>
      val button = new Label("", new ImageView(Resources.piecePathOf(Piece(piece, side))))
      button.setOnMouseEntered { _ => hover(button, Cursor.HAND, "lightgray") }
      button.setOnMouseExited { _ => hover(button, Cursor.DEFAULT, "none") }
      button.setOnMouseClicked { _ =>
        selectedPieceType = piece
        setResult(ButtonType.OK)
        close()
      }
      button
    }

    contentPane.getChildren.addAll(pieceButtons: _*)
    contentPane.setSpacing(10)
    contentPane
  }

  def hover(button: Label, cursor: Cursor, bgColor: String): Unit = {
    button.getScene.setCursor(cursor)
    button.setStyle("-fx-background-color:" + bgColor)
  }
}
