package views

import javafx.scene.control.ListView
import javafx.scene.layout.BorderPane

import controllers.DefaultBoardController
import engine.board.bitboards.Bitboard
import validators.MoveValidator
import views.menus.MenuBarView
import views.misc.CustomTitledPane

/**
  * Created by melvic on 9/12/18.
  */
class MainView extends BorderPane {
  val boardController = DefaultBoardController(Bitboard(), MoveValidator.validateMove)
  val boardView = boardController.boardView
  val historyView = new ListView[String]()
  historyView.getItems().addAll("e4 Nf3", "Be5 Ng7", "e4 Nf3", "Be5 Ng7", "e4 Nf3", "Be5 Ng7")
  historyView.setFocusTraversable(false)
  historyView.setStyle("-fx-font-size: 14")

  setTop(MenuBarView(boardView))
  setCenter(boardView)
  setRight(CustomTitledPane("History",historyView))
}
