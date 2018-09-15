package engine.movegen

import engine.board.{Board, Piece, Side}

/**
  * Created by melvic on 8/5/18.
  */
trait MoveGenerator[B <: Board] {
  def apply(board: B, sideToMove: Side): Stream[B]
}
