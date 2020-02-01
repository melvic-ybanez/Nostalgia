package com.github.melvic_ybanez.nostalgia.models

import com.github.melvic_ybanez.nostalgia.engine.board.Side

/**
  * Created by melvic on 9/15/18.
  */
sealed trait GameType
case object HumanVsHuman extends GameType
case class HumanVsComputer(humanSide: Side, level: Int) extends GameType
case class ComputerVsComputer(whiteLevel: Int, blackLevel: Int) extends GameType

