package engine.movegen

import java.engine.board.{Board, Piece}

/**
  * Created by melvic on 8/5/18.
  */
trait MoveGenerator[B <: Board] {
  def apply(board: B, sideToMove: Piece.Side): Stream[B]
}
