package ybanez.nostalgia.engine.movegen.bitboards

import ybanez.nostalgia.engine.board.bitboards.Bitboard.U64

/**
  * Created by melvic on 8/7/18.
  */
object KnightMoveGenerator extends NonSlidingMoveGenerator with PostShiftOneStep {
  def northNorthEast: U64 => U64 = north andThen northEast

  def northNorthWest: U64 => U64 = north andThen northWest

  def northEastEast: U64 => U64 = northEast andThen east

  def northWestWest: U64 => U64 = northWest andThen west

  def southEastEast: U64 => U64 = southEast andThen east

  def southWestWest: U64 => U64 = southWest andThen west

  def southSouthEast: U64 => U64 = south andThen southEast

  def southSouthWest: U64 => U64 = south andThen southWest

  override lazy val moves: Stream[U64 => U64] = Stream(
    northEastEast, northNorthEast, northNorthWest, northWestWest,
    southWestWest, southSouthWest, southSouthEast, southEastEast
  )
}
