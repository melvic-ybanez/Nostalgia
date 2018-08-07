package engine.movegen

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

