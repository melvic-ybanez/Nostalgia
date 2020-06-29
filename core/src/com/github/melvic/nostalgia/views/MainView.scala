package com.github.melvic.nostalgia.views

import com.github.melvic.nostalgia.controllers.DefaultMenuController
import com.github.melvic.nostalgia.engine.board.White
import com.github.melvic.nostalgia.main.Resources
import com.github.melvic.nostalgia.models.HumanVsHuman
import com.github.melvic.nostalgia.views.menus.MenuBarView
import javafx.geometry.{Insets, NodeOrientation, Orientation}
import javafx.scene.control.Separator
import scalafx.geometry.Insets
import scalafx.scene.layout.{BorderPane, FlowPane, HBox}

/**
  * Created by melvic on 9/12/18.
  */
class MainView extends BorderPane {
  val menuController = new DefaultMenuController
  val boardController = menuController.boardController
  val boardView = boardController.boardView
  val historyView = boardController.historyView

  historyView.setStyle(boardView.getStyle)
  stylesheets.add(Resources.styleSheets("nostalgia"))
  top = MenuBarView(boardView)
  center = new HBox {
    children.addAll(boardView, historyView)
    style = boardView.getStyle
    spacing = 33
  }

  boardController.newGame(White, HumanVsHuman)
}
