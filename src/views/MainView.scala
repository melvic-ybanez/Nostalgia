package views

import javafx.scene.layout.BorderPane

import controllers.DefaultBoardController
import engine.board.bitboards.Bitboard
import validators.DefaultMoveValidator
import views.menus.MenuBarView

/**
  * Created by melvic on 9/12/18.
  */
class MainView extends BorderPane {
  val boardController = DefaultBoardController(Bitboard(), new DefaultMoveValidator)
  val boardView = boardController.boardView
  setTop(MenuBarView(boardView))
  setCenter(boardView)
}
