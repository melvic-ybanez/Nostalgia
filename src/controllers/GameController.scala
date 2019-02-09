package controllers

import javafx.animation.AnimationTimer

import models.{HumanVsHuman, _}


/**
  * Created by melvic on 2/10/19.
  */
trait GameController {
  def boardController: BoardController
  def play(gameType: GameType): Unit

  private var _gameState: GameState = HumanToMove
  def gameState = _gameState
  def gameState_=(gameState: GameState): Unit = _gameState = gameState
}

case class DefaultGameController(boardController: BoardController) extends GameController {
  val boardView = boardController.boardView

  override def play(gameType: GameType): Unit = {
    if (gameState == HumanToMove)
      boardView.registerListeners()

    val timer: AnimationTimer = new AnimationTimer() {
      override def handle(now: Long) = gameState match {
        case HumanToMove =>   // keep waiting for inputs
        case ComputerToMove =>
          boardController.computerMove()
        case PreAnimation =>
          // Do not accept inputs while animating.
          boardView.removeListeners()

          boardView.animateMove()
          gameState = Animation
        case Animation =>   // keep ignoring inputs
        case PostAnimation =>
          gameType match {
            case HumanVsHuman =>
              // All players are humans. Accept inputs.
              boardView.registerListeners()
            case HumanVsComputer(humanSide) if humanSide == boardController.sideToMove =>
              // The next player is a human. Accept inputs.
              boardView.registerListeners()
          }
        case GameOver(result) =>
          result match {
            case CheckMate(winner) => boardView.showCheckmateDialog(winner)
          }
          this.stop()
      }
    }
    timer.start()
  }
}
