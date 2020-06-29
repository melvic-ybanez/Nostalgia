package com.github.melvic.nostalgia.views.menus

import com.github.melvic.nostalgia.main.Main
import com.github.melvic.nostalgia.views.boards.BoardView
import javafx.scene.control.{Menu, MenuItem}
import javafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}
import scalafx.scene.control.MenuBar

/**
  * Created by melvic on 9/15/18.
  */
case class MenuBarView(boardView: BoardView) extends MenuBar {
  menus.addAll(createFileMenu, GameMenu(boardView.boardController))

  def createFileMenu = {
    val fileMenu = new Menu("File")

    val exit = new MenuItem("Exit")
    exit.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.META_DOWN))
    exit.setOnAction(_ => Main.exit())

    fileMenu.getItems.addAll(exit)

    fileMenu
  }
}
