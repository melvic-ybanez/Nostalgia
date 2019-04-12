package com.github.melvic_ybanez.nostalgia.views

import javafx.geometry.Orientation
import javafx.scene.control.Separator
import javafx.scene.layout.{BorderPane, FlowPane}

import com.github.melvic_ybanez.nostalgia.controllers.DefaultMenuController
import com.github.melvic_ybanez.nostalgia.engine.board.White
import com.github.melvic_ybanez.nostalgia.models.HumanVsHuman
import com.github.melvic_ybanez.nostalgia.views.menus.MenuBarView
import com.github.melvic_ybanez.nostalgia.views.misc.CustomTitledPane

/**
  * Created by melvic on 9/12/18.
  */
class MainView extends BorderPane {
  val menuController = new DefaultMenuController
  val boardController = menuController.boardController
  val boardView = boardController.boardView
  val historyView = boardController.historyView

  //val historyPane = CustomTitledPane("History", historyView)
  //historyPane.setPadding(boardView.getPadding)
  //setStyle(boardView.getStyle)

  val historyPaneWrapper = {
    val centerPane = new FlowPane

    historyView.setStyle(boardView.getStyle)
    centerPane.setStyle(boardView.getStyle)

    centerPane.getChildren.addAll(new Separator(Orientation.VERTICAL), historyView)
    centerPane
  }

  setTop(MenuBarView(boardView))
  setCenter(boardView)
  setRight(historyPaneWrapper)

  boardController.newGame(White, HumanVsHuman)
}
