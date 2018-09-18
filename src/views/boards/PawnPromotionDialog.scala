package views.boards

import javafx.scene.control.{Button, ButtonType, Dialog, Label}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{GridPane, HBox}

import engine.board._
import main.Resources

/**
  * Created by melvic on 9/18/18.
  */
case class PawnPromotionDialog(side: Side) extends Dialog[ButtonType] {
  var selectedPieceType: PieceType = Queen

  setTitle("Pawn Promtion")

  getDialogPane.setContent {
    val contentPane = new HBox

    val officers = Queen :: Rook :: Bishop :: Knight :: Nil
    val pieceButtons = officers.map { piece =>
      val button = new Button("", new ImageView(Resources.piecePathOf(Piece(piece, side))))
      button.setOnAction(e => selectedPieceType = piece)
      button
    }

    contentPane.getChildren.addAll(pieceButtons: _*)
    contentPane
  }

  getDialogPane.getButtonTypes.addAll(ButtonType.OK)
}
