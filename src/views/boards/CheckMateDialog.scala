package views.boards

import javafx.scene.control.{ButtonType, Dialog, Label}

import engine.board.Side

/**
  * Created by melvic on 9/11/18.
  */
class CheckMateDialog extends Dialog[ButtonType] {
  private val label = new Label

  getDialogPane.setContent(label)
  getDialogPane.getButtonTypes.add(ButtonType.OK)

  def show(winner: Side): Unit = {
    label.setText(winner + " won by Checkmate.")
    showAndWait()
  }
}
