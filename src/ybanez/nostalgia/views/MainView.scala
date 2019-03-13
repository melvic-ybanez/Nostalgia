package ybanez.nostalgia.views

import javafx.scene.layout.BorderPane

import ybanez.nostalgia.controllers.DefaultMenuController
import ybanez.nostalgia.engine.board.White
import ybanez.nostalgia.models.HumanVsHuman
import ybanez.nostalgia.views.menus.MenuBarView
import ybanez.nostalgia.views.misc.CustomTitledPane

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
