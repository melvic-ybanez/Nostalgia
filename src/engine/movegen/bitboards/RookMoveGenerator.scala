package engine.movegen.bitboards

import engine.board.bitboards.Transformers

/**
  * Created by melvic on 9/23/18.
  */
object RookMoveGenerator extends SlidingMoveGenerator {
  def north: Slide = positiveRay(fileMask)

  def south: Slide = negativeRay()(fileMask)

  def east: Slide = positiveRay(rankMask)

  def west: Slide = negativeRay(Transformers.horizontalMirror)(rankMask)

  override def moves = Stream(north, south, east, west)
}
