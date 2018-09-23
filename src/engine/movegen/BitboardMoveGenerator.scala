package engine.movegen

import engine.board.Side
import engine.board.bitboards.Bitboard
import engine.board.bitboards.Bitboard.U64
import engine.movegen.BitboardMoveGenerator._

/**
  * Created by melvic on 8/5/18.
  */
trait BitboardMoveGenerator {
  type Generator[A] = (Bitboard, Int, Side) => A
  type StreamGen[A] = Generator[Stream[A]]
  type WithMove[A] = (A, MoveType)

  def destinationBitsets: StreamGen[WithMove[U64]]

  def validDestinationBitsets: StreamGen[WithMove[U64]] = destinationBitsets(_, _, _)
    .filter { case (board, _) => Bitboard.isNonEmptySet(board) }

  def attackBitsets: StreamGen[U64] = validDestinationBitsets(_, _, _).filter {
    case (_, Attack) => true
    case _ => false
  } map { case (bitboard, _) => bitboard }

  def destinations: StreamGen[WithMove[Int]] = validDestinationBitsets(_, _, _)
    .map(withMoveType(Bitboard.oneBitIndex))

  def emptyOrOpponent(emptySquares: U64, opponents: U64): U64 => U64 = _ & (emptySquares | opponents)
}

object BitboardMoveGenerator {
  def withMoveType[A, B](f: A => B)(pair: (A, MoveType)) = pair match {
    case (value, moveType) => (f(value), moveType)
  }
}

