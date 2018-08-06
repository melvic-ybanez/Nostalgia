package scala.engine.movegen.scala

import java.engine.board.Board

import scala.engine.board.bitboards.Bitboard.{SetwiseOperator, U64}

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
  object Masks {
    val NotAFile = 0xfefefefefefefefeL
    val NotHFile = 0x7f7f7f7f7f7f7f7fL
  }

  def north = bitboard => bitboard << Board.SIZE
  def south = bitboard => bitboard >>> Board.SIZE
  def east = bitboard => (bitboard << 1) & Masks.NotAFile
  def west = bitboard => (bitboard >>> 1) & Masks.NotHFile
}