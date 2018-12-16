package engine.movegen

/**
  * Created by melvic on 9/23/18.
  */
object RookMoveGenerator extends SlidingMoveGenerator {
  def north: Slide = positiveRay(fileMask)

  def south: Slide = negativeRay(fileMask)

  def east: Slide = positiveRay(rankMask)

  def west: Slide = negativeRay(rankMask)

  override def moves = ???
}
