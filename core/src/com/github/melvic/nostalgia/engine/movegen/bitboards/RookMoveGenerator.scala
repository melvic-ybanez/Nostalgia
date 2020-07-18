package com.github.melvic.nostalgia.engine.movegen.bitboards

import com.github.melvic.nostalgia.engine.board.bitboards.Transformers

/**
  * Created by melvic on 9/23/18.
  */
object RookMoveGenerator extends SlidingMoveGenerator {
  def north: Slide = positiveRay(fileMask)

  def south: Slide = negativeRay()(fileMask)

  def east: Slide = positiveRay(rankMask)

  def west: Slide = negativeRay(Transformers.horizontalMirror)(rankMask)

  override def moves = List(north, south, east, west)
}
