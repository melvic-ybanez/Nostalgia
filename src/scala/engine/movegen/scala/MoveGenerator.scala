package scala.engine.movegen.scala

import java.engine.board.{Board, Piece}

import scala.engine.board.java.Piece
import java.engine.board.bitboards.Piece

/**
  * Created by melvic on 8/5/18.
  */
trait MoveGenerator[B <: Board] {
  def apply(board: B, sideToMove: Piece.Side): Stream[B]
}
