package engine.movegen

import engine.board.bitboards.Bitboard
import engine.board.bitboards.Bitboard.U64
import engine.board.bitboards.Implicits._

/**
  * Created by melvic on 8/5/18.
  */
trait NonSlidingMoveGenerator extends BitboardMoveGenerator {
  type NonSlidingMove = (U64, U64, U64) => Long

  def allMoves: Stream[WithMove[NonSlidingMove]]

  def destinationBitsets: GeneratorStream[WithMove[U64]] = (bitboard, source, sideToMove) => {
    val pieces = Bitboard.singleBitset(source)
    val emptySquares = bitboard.emptySquares
    val opponents = bitboard.opponents(sideToMove)

    allMoves.map { case (move, moveType) => (move(pieces, emptySquares, opponents), moveType) }
  }
}
