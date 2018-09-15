package views.menus

import javafx.scene.control.{ButtonType, Menu, MenuItem, SeparatorMenuItem}
import javafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}

import controllers.BoardController

/**
  * Created by melvic on 9/15/18.
  */
case class GameMenu(boardController: BoardController) extends Menu {
  val gameDialog = new NewGameDialog
  val newGameItem = new MenuItem("New Game...")
  newGameItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.META_DOWN))
  newGameItem.setOnAction(_ => {
    gameDialog.showAndWait().ifPresent(result => {
      if (result == ButtonType.APPLY) {
        boardController.newGame(gameDialog.sideToPlay)
      }
    })
  })

  val rotateGameItem = new MenuItem("Rotate Board")
  rotateGameItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.META_DOWN))
  rotateGameItem.setOnAction(_ => {
    boardController.rotate()
  })

  getItems.addAll(newGameItem, new SeparatorMenuItem, rotateGameItem)
}
