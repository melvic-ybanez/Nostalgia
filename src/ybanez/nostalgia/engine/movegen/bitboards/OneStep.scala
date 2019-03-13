package ybanez.nostalgia.engine.movegen.bitboards

import ybanez.nostalgia.engine.board.Board
import ybanez.nostalgia.engine.board.bitboards.Bitboard.U64
import ybanez.nostalgia.engine.utils.Masks

/**
  * Created by melvic on 8/5/18.
  */
trait OneStep {
  import OneStep._

  def north: Step
  def south: Step
  def east: Step
  def west: Step

  def northEast: Step = north andThen east
  def northWest: Step = north andThen west
  def southEast: Step = south andThen east
  def southWest: Step = south andThen west
}

object OneStep {
  type Step = U64 => U64
}

trait PostShiftOneStep extends OneStep {
  def north = bitboard => bitboard << Board.Size
  def south = bitboard => bitboard >>> Board.Size
  def east = bitboard => (bitboard << 1) & Masks.NotAFile
  def west = bitboard => (bitboard >>> 1) & Masks.NotHFile
}