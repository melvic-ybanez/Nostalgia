package com.github.melvic_ybanez.nostalgia.models

import com.github.melvic_ybanez.nostalgia.engine.board.White

/**
  * Created by melvic on 9/15/18.
  */
object Preferences {
  object Defaults {
    lazy val gameType = HumanVsHuman
    lazy val sideToPlay = White
  }
}
