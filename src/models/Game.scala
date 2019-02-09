package models

import engine.board.Side

/**
  * Created by melvic on 9/15/18.
  */
sealed trait GameType
case object HumanVsHuman extends GameType
case class HumanVsComputer(humanSide: Side) extends GameType

sealed trait GameState
case object HumanToMove extends GameState
case object ComputerToMove extends GameState
case object PreAnimation extends GameState
case object Animation extends GameState
case object PostAnimation extends GameState
case class GameOver(result: GameResult) extends GameState

sealed trait GameResult
case class CheckMate(winnerSide: Side) extends GameResult

