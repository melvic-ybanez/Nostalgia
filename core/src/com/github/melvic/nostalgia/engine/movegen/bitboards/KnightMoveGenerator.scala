package com.github.melvic.nostalgia.engine.movegen.bitboards

import com.github.melvic.nostalgia.engine.movegen.bitboards.OneStep.Step

/**
  * Created by melvic on 8/7/18.
  */
object KnightMoveGenerator extends NonSlidingMoveGenerator with PostShiftOneStep {
  def northNorthEast: Step = north andThen northEast

  def northNorthWest: Step = north andThen northWest

  def northEastEast: Step = northEast andThen east

  def northWestWest: Step = northWest andThen west

  def southEastEast: Step = southEast andThen east

  def southWestWest: Step = southWest andThen west

  def southSouthEast: Step = south andThen southEast

  def southSouthWest: Step = south andThen southWest

  override lazy val moves: Stream[Step] = Stream(
    northEastEast, northNorthEast, northNorthWest, northWestWest,
    southWestWest, southSouthWest, southSouthEast, southEastEast
  )
}
