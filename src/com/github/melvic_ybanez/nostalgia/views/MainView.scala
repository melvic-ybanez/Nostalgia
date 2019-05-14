package com.github.melvic_ybanez.nostalgia.views

import javafx.geometry.{Insets, Orientation}
import javafx.scene.control.Separator
import javafx.scene.layout.{AnchorPane, BorderPane, FlowPane, HBox}
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

  historyView.setStyle(boardView.getStyle)

  setTop(MenuBarView(boardView))
  setCenter {
    val contentPane = new HBox
    contentPane.getChildren.addAll(boardView, new Separator(Orientation.VERTICAL), historyView)
    contentPane.setStyle(boardView.getStyle)
    HBox.setMargin(historyView, new Insets(0))
    contentPane
  }

  boardController.newGame(White, HumanVsHuman)
}
