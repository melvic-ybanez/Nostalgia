package com.github.melvic.nostalgia.engine.board.piece

sealed trait Side

object Side {
  case object White extends Side
  case object Black extends Side
}