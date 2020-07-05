package com.github.melvic.nostalgia.engine.movegen

import com.github.melvic.nostalgia.engine.board.Board
import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard

/**
  * Created by melvic on 8/6/18.
  */
sealed trait File {
  def apply(rank: Rank): Location = Location(this, rank)
}

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

case class Location(file: File, rank: Rank) {
  def to(destination: Location): Move[Location] = Move(this, destination)
}

object Location {
  lazy val Files: List[File] = A :: B :: C :: D :: E :: F :: G :: H :: Nil
  lazy val Ranks: List[Rank] = _1 :: _2 :: _3 :: _4 :: _5 :: _6 :: _7 :: _8 :: Nil

  implicit class LocationOps(location: Location) {
    def toBitPosition: Int = Bitboard.toBitPosition(location)
  }

  implicit def fileToInt(file: File): Int = Location.Files.indexOf(file)

  implicit def rankToInt(rank: Rank): Int = Location.Ranks.indexOf(rank)

  implicit def intToFile(i: Int): File = Location.Files(i)

  implicit def intToRank(i: Int): Rank = Location.Ranks(i)

  @deprecated("use the extension method LocationOps.toBitPosition instead")
  implicit def locationToInt(location: Location): Int = Bitboard.toBitPosition(location)

  implicit def intToLocation(position: Int): Location = Location(
    Bitboard.fileOf(position), Bitboard.rankOf(position))

  def locateForView(row: Int, col: Int) =
    Location(Location.Files(col), Location.Ranks(Board.Size - 1 - row))
}

