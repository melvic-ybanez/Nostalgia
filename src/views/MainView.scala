package views

import javafx.scene.control.ListView
import javafx.scene.layout.BorderPane

import controllers.DefaultBoardController
import engine.board.bitboards.Bitboard
import validators.MoveValidator
import views.boards.HistoryView
import views.menus.MenuBarView
import views.misc.CustomTitledPane

/**
  * Created by melvic on 9/12/18.
  */
class MainView extends BorderPane {
  val boardController = DefaultBoardController(Bitboard(), MoveValidator.validateMove)
  val boardView = boardController.boardView
  val historyView = boardController.historyView

  val historyPane = CustomTitledPane("History", historyView)
  historyPane.setPadding(boardView.getPadding)
  setStyle(boardView.getStyle)

  setTop(MenuBarView(boardView))
  setCenter(boardView)
  setRight(historyPane)
}
