package com.github.melvic.nostalgia.engine.api.movegen

import cats.Bifunctor
import com.github.melvic.nostalgia.engine.base.{Board, Square}
import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard

final case class Location(file: File, rank: Rank)

object Location {
  type MFile = File
  type MRank = Rank

  def locateForView(row: Int, col: Int) =
    Location(Files(col), Ranks(Board.Size - 1 - row))

  implicit object locationSquare extends Square[Location] {
    override type File = MFile
    override type Rank = MRank

    override def file(square: Location) = square.file

    override def rank(square: Location) = square.rank
  }

  trait implicits {
    implicit class LocationOps(square: Location) {
      def to(destination: Location): Move = Move.normal(square, destination)

      def toBitPosition: Int = Bitboard.toBitPosition(square)
    }
  }
}
