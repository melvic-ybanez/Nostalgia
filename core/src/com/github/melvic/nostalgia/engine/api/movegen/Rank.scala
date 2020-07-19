package com.github.melvic.nostalgia.engine.api.movegen

sealed trait Rank

object Rank {
  case object _1 extends Rank
  case object _2 extends Rank
  case object _3 extends Rank
  case object _4 extends Rank
  case object _5 extends Rank
  case object _6 extends Rank
  case object _7 extends Rank
  case object _8 extends Rank

  lazy val Ranks: List[Rank] = _1 :: _2 :: _3 :: _4 :: _5 :: _6 :: _7 :: _8 :: Nil

  trait implicits {
    implicit class RankOps(rank: Rank) {
      def toInt: Int = Ranks.indexOf(rank)
    }
  }
}
