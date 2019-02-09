package views

import javafx.scene.layout.BorderPane

import controllers.DefaultMenuController
import engine.board.White
import models.HumanVsHuman
import views.menus.MenuBarView
import views.misc.CustomTitledPane

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
