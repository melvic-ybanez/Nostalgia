package engine.movegen

import engine.board.Piece
import engine.board.bitboards.Bitboard
import engine.board.bitboards.Bitboard.U64

/**
  * Created by melvic on 9/23/18.
  */
object BishopMoveGenerator extends SlidingMoveGenerator {
  def positiveDiagonal: Slide = positiveRay(diagonalMask)

  def negativeDiagonal: Slide = negativeRay(diagonalMask)

  def positiveAntiDiagonal: Slide = positiveRay(antiDiagonalMask)

  def negativeAntiDiagonal: Slide = negativeRay(antiDiagonalMask)

  override def destinationBitsets: StreamGen[WithMove[U64]] = { (board, source, side) =>
    Stream(positiveDiagonal, negativeDiagonal, positiveAntiDiagonal, negativeDiagonal) flatMap { slide =>
      val moveBitset = slide(source, board.occupied)
      val blocker = board.occupied & moveBitset

      // remove the blocker from the set of valid destinations if it's not an enemy and is not empty
      val validMoveBitSet = board(Bitboard.oneBitIndex(blocker)) match {
        case Some(Piece(_, blockerSide)) if blockerSide == side =>
          moveBitset ^ blocker
        case _ => moveBitset
      }

      Bitboard.serializeToStream(validMoveBitSet).map((_, Attack))
    }
  }
}
