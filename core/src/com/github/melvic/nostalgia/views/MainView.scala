package com.github.melvic.nostalgia.views

import com.github.melvic.nostalgia.controllers.DefaultMenuController
import com.github.melvic.nostalgia.engine.board.White
import com.github.melvic.nostalgia.main.Resources
import com.github.melvic.nostalgia.models.HumanVsHuman
import com.github.melvic.nostalgia.views.menus.MenuBarView
import javafx.geometry.{Insets, NodeOrientation, Orientation}
import javafx.scene.control.Separator
import javafx.scene.layout.{BorderPane, HBox}
import scalafx.scene.layout.FlowPane

/**
  * Created by melvic on 9/12/18.
  */
class MainView extends BorderPane {
  val menuController = new DefaultMenuController
  val boardController = menuController.boardController
  val boardView = boardController.boardView
  val historyView = boardController.historyView

  historyView.setStyle(boardView.getStyle)
  getStylesheets().add(Resources.styleSheets("nostalgia"))

  setTop(MenuBarView(boardView))
  setCenter {
    /*
    val contentPane = new HBox()
    contentPane.getChildren.addAll(boardView, new Separator(Orientation.VERTICAL), historyView)
    contentPane.setStyle(boardView.getStyle)
    HBox.setMargin(historyView, new Insets(0))
    contentPane
     */
    boardView
  }

  boardController.newGame(White, HumanVsHuman)
}
