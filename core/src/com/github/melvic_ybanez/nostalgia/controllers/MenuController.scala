package com.github.melvic_ybanez.nostalgia.controllers

import com.github.melvic_ybanez.nostalgia.engine.board.bitboards.Bitboard
import com.github.melvic_ybanez.nostalgia.validators.MoveValidator
import com.github.melvic_ybanez.nostalgia.views.menus.GameMenu

/**
  * Created by melvic on 1/24/19.
  */
trait MenuController {
  def gameMenu: GameMenu
  def boardController: GameController

  def newGame(): Unit
}

class DefaultMenuController extends MenuController {
  val boardController = DefaultGameController(this, Bitboard(), MoveValidator.validateMove)
  val gameMenu = GameMenu(boardController)

  def newGame(): Unit = {
    gameMenu.showNewGameDialog()
  }
}
