package engine.movegen

import java.engine.board.Board

import engine.board.bitboards.Bitboard.U64
import engine.utils.Masks

/**
  * Created by melvic on 8/5/18.
  */
trait OneStep {
  type Step = U64 => U64

  def north: Step
  def south: Step
  def east: Step
  def west: Step

  def northEast: Step = north andThen east
  def northWest: Step = north andThen west
  def southEast: Step = south andThen east
  def southWest: Step = south andThen west
}

trait PostShiftOneStep extends OneStep {
  def north = bitboard => bitboard << Board.SIZE
  def south = bitboard => bitboard >>> Board.SIZE
  def east = bitboard => (bitboard << 1) & Masks.NotAFile
  def west = bitboard => (bitboard >>> 1) & Masks.NotHFile
}