package ybanez.nostalgia.models

import ybanez.nostalgia.engine.board.White

/**
  * Created by melvic on 9/15/18.
  */
object Preferences {
  object Defaults {
    lazy val gameType = HumanVsHuman
    lazy val sideToPlay = White
  }
}
