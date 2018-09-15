package models

/**
  * Created by melvic on 9/15/18.
  */
sealed trait GameType
case object HumanVsHuman extends GameType
case object HumanVsComputer extends GameType

