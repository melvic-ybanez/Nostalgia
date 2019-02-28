package models

import engine.board.Side

/**
  * Created by melvic on 9/15/18.
  */
sealed trait GameType
case object HumanVsHuman extends GameType
case class HumanVsComputer(humanSide: Side, level: Int) extends GameType

