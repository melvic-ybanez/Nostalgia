package engine.movegen.bitboards

import engine.board.Side
import engine.board.bitboards.Bitboard
import engine.board.bitboards.Bitboard.U64
import engine.movegen.bitboards.BitboardMoveGenerator.{withMoveType}
import engine.movegen.{Attack, MoveType}

/**
  * Created by melvic on 8/5/18.
  */
trait BitboardMoveGenerator {
  /**
    * A function that takes a bitboard, an integer and the side to move,
    * and generates an instance of A, where A is a type parameter.
    */
  type Generator[A] = (Bitboard, Int, Side) => A

  /**
    * A stream generator.
    * @tparam A the type of the elements in the generated stream.
    */
  type StreamGen[A] = Generator[Stream[A]]

  /**
    * A pair comprised of a value of type A and a particular move type.
    */
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

