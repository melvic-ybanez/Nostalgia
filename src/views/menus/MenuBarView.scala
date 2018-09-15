package views.menus

import javafx.application.Platform
import javafx.scene.control.{Menu, MenuBar, MenuItem}
import javafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}

import views.boards.BoardView

/**
  * Created by melvic on 9/15/18.
  */
case class MenuBarView(boardView: BoardView)  extends MenuBar {
  getMenus.addAll(createFileMenu, GameMenu(boardView.boardController))

  def createFileMenu = {
    val fileMenu = new Menu("File")

    val exit = new MenuItem("Exit")
    exit.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.META_DOWN))
    exit.setOnAction(_ => Platform.exit())

    fileMenu.getItems.addAll(exit)

    fileMenu
  }
}
