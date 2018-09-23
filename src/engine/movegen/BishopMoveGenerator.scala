package engine.movegen

import engine.board.bitboards.Bitboard.U64

/**
  * Created by melvic on 9/23/18.
  */
object BishopMoveGenerator extends SlidingMoveGenerator {
  def positiveDiagonal: Slide = positiveRay(diagonalMask)

  def negativeDiagonal: Slide = negativeRay(diagonalMask)

  def positiveAntiDiagonal: Slide = positiveRay(antiDiagonalMask)

  def negativeAntiDiagonal: Slide = negativeRay(antiDiagonalMask)

  override def destinationBitsets = ???
}
