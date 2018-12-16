package engine.movegen.bitboards

import engine.board.bitboards.Bitboard.U64

/**
  * Created by melvic on 9/23/18.
  */
object KingMoveGenerator extends NonSlidingMoveGenerator with PostShiftOneStep {
  lazy val moves: Stream[U64 => U64] = Stream(
    north, south, east, west, northEast, northWest, southEast, southWest
  )
}
