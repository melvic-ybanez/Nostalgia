package com.github.melvic.nostalgia.engine.api.movegen

import com.github.melvic.nostalgia.engine.base.Board
import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard

final case class Coordinate(file: File, rank: Rank) {
  def to(destination: Coordinate): Move = Move.normal(this, destination)
}

object Coordinate {
  implicit class CoordinateOps(coordinate: Coordinate) {
    def toBitPosition: Int = Bitboard.toBitPosition(coordinate)
  }

  implicit def intToFile(i: Int): File = Coordinate.Files(i)

  implicit def intToRank(i: Int): Rank = Coordinate.Ranks(i)

  @deprecated("use the extension method LocationOps.toBitPosition instead")
  implicit def locationToInt(location: Coordinate): Int = Bitboard.toBitPosition(location)

  implicit def intToLocation(position: Int): Coordinate = Coordinate(
    Bitboard.fileOf(position), Bitboard.rankOf(position))

  def locateForView(row: Int, col: Int) =
    Coordinate(Coordinate.Files(col), Coordinate.Ranks(Board.Size - 1 - row))
}

