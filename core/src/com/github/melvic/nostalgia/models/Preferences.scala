package com.github.melvic.nostalgia.models

import com.github.melvic.nostalgia.engine.board.White

/**
  * Created by melvic on 9/15/18.
  */
object Preferences {
  object Defaults {
    lazy val gameType = HumanVsHuman
    lazy val sideToPlay = White
  }
}
