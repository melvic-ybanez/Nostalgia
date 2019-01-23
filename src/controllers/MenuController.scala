package controllers

import engine.board.bitboards.Bitboard
import validators.MoveValidator
import views.menus.GameMenu

/**
  * Created by melvic on 1/24/19.
  */
trait MenuController {
  def gameMenu: GameMenu
  def boardController: BoardController

  def newGame(): Unit
}

class DefaultMenuController extends MenuController {
  val boardController = DefaultBoardController(this, Bitboard(), MoveValidator.validateMove)
  val gameMenu = GameMenu(boardController)

  def newGame(): Unit = {
    gameMenu.showNewGameDialog()
  }
}
