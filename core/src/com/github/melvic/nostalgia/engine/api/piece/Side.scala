package com.github.melvic.nostalgia.engine.api.piece

sealed trait Side

object Side {
  case object White extends Side
  case object Black extends Side

  lazy val all: List[Side] = List(White, Black)
}