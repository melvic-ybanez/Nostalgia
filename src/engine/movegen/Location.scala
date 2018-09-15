package engine.movegen

import engine.utils.Implicits.Locations._

/**
  * Created by melvic on 8/6/18.
  */
sealed trait File
case object A extends File
case object B extends File
case object C extends File
case object D extends File
case object E extends File
case object F extends File
case object G extends File
case object H extends File

sealed trait Rank
case object _1 extends Rank
case object _2 extends Rank
case object _3 extends Rank
case object _4 extends Rank
case object _5 extends Rank
case object _6 extends Rank
case object _7 extends Rank
case object _8 extends Rank

case class Location(file: File, rank: Rank)

object Location {
  lazy val Files: List[File] = A :: B :: C :: D :: E :: F :: G :: H :: Nil
  lazy val Ranks: List[Rank] = _1 :: _2 :: _3 :: _4 :: _5 :: _6 :: _7 :: _8 :: Nil
}

