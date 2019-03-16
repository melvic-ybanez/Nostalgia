package com.github.melvic_ybanez.nostalgia.views

import javafx.scene.layout.BorderPane

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

  val historyPane = CustomTitledPane("History", historyView)
  historyPane.setPadding(boardView.getPadding)
  setStyle(boardView.getStyle)

  setTop(MenuBarView(boardView))
  setCenter(boardView)
  setRight(historyPane)

  boardController.newGame(White, HumanVsHuman)
}
