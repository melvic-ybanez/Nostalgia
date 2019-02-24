package views.menus

import java.lang.Boolean
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.control._
import javafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}

import controllers.BoardController

/**
  * Created by melvic on 9/15/18.
  */
case class GameMenu(boardController: BoardController) extends Menu {
  setText("Game")

  val gameDialog = new NewGameDialog
  val newGameMI = new MenuItem("New Game...")
  newGameMI.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.META_DOWN))
  newGameMI.setOnAction { _ => showNewGameDialog() }

  val rotateGameMI = new MenuItem("Rotate Board")
  rotateGameMI.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN))
  rotateGameMI.setOnAction(_ => boardController.rotate())

  val resignMI = new MenuItem("Resign")
  resignMI.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.META_DOWN))
  resignMI.setOnAction(_ => boardController.boardView.showResignConfirmationDialog())

  getItems.addAll(newGameMI, new SeparatorMenuItem, rotateGameMI, resignMI)

  def showNewGameDialog(): Unit = {
    gameDialog.showAndWait().ifPresent { result =>
      if (result == ButtonType.OK)
        boardController.newGame(gameDialog.sideToPlay, gameDialog.gameType)
    }
  }
}
