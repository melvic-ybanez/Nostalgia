package engine.movegen

import engine.board.bitboards.Bitboard
import engine.board.bitboards.Bitboard.U64
import engine.movegen.KnightMoveGenerator.emptyOrOpponent

/**
  * Created by melvic on 8/5/18.
  */
trait NonSlidingMoveGenerator extends BitboardMoveGenerator {
  def moves: Stream[U64 => U64]

  def destinationBitsets: StreamGen[WithMove[U64]] = { (board, source, side) =>
    val pieces = Bitboard.singleBitset(source)
    moves map { f =>
      val dest = emptyOrOpponent(board.emptySquares, board.opponents(side))(f(pieces))
      (dest, Attack)
    }
  }
}
