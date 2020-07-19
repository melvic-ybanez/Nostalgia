package com.github.melvic.nostalgia.engine.api.movegen

sealed trait File {
  def apply(rank: Rank): Location = Location(this, rank)
}

object File {
  case object A extends File
  case object B extends File
  case object C extends File
  case object D extends File
  case object E extends File
  case object F extends File
  case object G extends File
  case object H extends File

  lazy val Files: List[File] = A :: B :: C :: D :: E :: F :: G :: H :: Nil

  trait implicits {
    implicit class FileOps(file: File) {
      def toInt: Int = Files.indexOf(file)
    }
  }
}