package com.github.melvic.nostalgia.views.menus

import com.github.melvic.nostalgia.controllers.GameController
import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableBooleanValue
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.control._
import javafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}

/**
  * Created by melvic on 9/15/18.
  */
case class GameMenu(gameController: GameController) extends Menu {
  setText("Game")

  val gameDialog = new NewGameDialog

  val newGameMI = (createMenuItem("New Game...")
      andThen addAccelerator(KeyCode.N, KeyCombination.META_DOWN)) { _ =>
    showNewGameDialog()
  }

  val rotateGameMI = (createMenuItem("Rotate Board")
      andThen addAccelerator(KeyCode.R, KeyCombination.ALT_DOWN)) { _ =>
    gameController.rotate()
  }

  val resignMI = (createMenuItem("Resign")
      andThen addAccelerator(KeyCode.R, KeyCombination.META_DOWN)
      andThen disableWhen(gameController.gameOnGoingProperty.not())) { _ =>
    gameController.boardView.showResignConfirmationDialog()
  }

  val undoMI = (createMenuItem("Undo")
      andThen addAccelerator(KeyCode.Z, KeyCombination.CONTROL_DOWN)
      andThen disableWhen(Bindings.isEmpty(gameController.historyBoards))) { _ =>
    gameController.undo
  }

  val redoMI = (createMenuItem("Redo")
      andThen addAccelerator(KeyCode.Y, KeyCombination.CONTROL_DOWN)
      andThen disableWhen(Bindings.isEmpty(gameController.undoneBoards))) { _ =>
    gameController.redo
  }

  getItems.addAll(newGameMI, new SeparatorMenuItem,
    resignMI, new SeparatorMenuItem,
    undoMI, redoMI, rotateGameMI)

  def showNewGameDialog(): Unit = {
    gameDialog.show()
    gameDialog.setOnHidden { result =>
      val result = gameDialog.getResult
      if (result == ButtonType.OK)
        gameController.newGame(gameDialog.sideToPlay, gameDialog.gameType)
    }
  }

  def createMenuItem(name: String): EventHandler[ActionEvent] => MenuItem = { action =>
    val menuItem = new MenuItem(name)
    menuItem.setOnAction(action)
    menuItem
  }

  def addAccelerator(code: KeyCode, combination: KeyCombination.Modifier)(menuItem: MenuItem) = {
    menuItem.setAccelerator(new KeyCodeCombination(code, combination))
    menuItem
  }

  def disableWhen(observable: ObservableBooleanValue)(menuItem: MenuItem) = {
    menuItem.disableProperty().bind(observable)
    menuItem
  }
}
