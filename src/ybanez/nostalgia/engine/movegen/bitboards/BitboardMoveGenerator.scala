package ybanez.nostalgia.engine.movegen.bitboards

import ybanez.nostalgia.engine.board._
import ybanez.nostalgia.engine.board.bitboards.Bitboard
import ybanez.nostalgia.engine.board.bitboards.Bitboard.U64
import ybanez.nostalgia.engine.movegen.Move.BitboardMove
import ybanez.nostalgia.engine.movegen.bitboards.BitboardMoveGenerator.withMoveType
import ybanez.nostalgia.engine.movegen.{Attack, Move, MoveType}

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

  def nonEmptyDestinationBitsets: StreamGen[WithMove[U64]] = destinationBitsets(_, _, _)
    .filter { case (bitboard, _) => Bitboard.isNonEmptySet(bitboard) }

  def attackBitsets: StreamGen[U64] = nonEmptyDestinationBitsets(_, _, _).filter {
    case (_, Attack) => true
    case _ => false
  } map { case (bitboard, _) => bitboard }

  def destinations: StreamGen[WithMove[Int]] = nonEmptyDestinationBitsets(_, _, _)
    .map(withMoveType(_)(Bitboard.bitScan))

  def validMoves: StreamGen[BitboardMove] = { (bitboard, source, side) =>
    destinations(bitboard, source, side).map { case (destination, moveType) =>
      Move[Int](source, destination, moveType)
    }
  }

  def emptyOrOpponent(emptySquares: U64, opponents: U64): U64 => U64 = _ & (emptySquares | opponents)
}

object BitboardMoveGenerator {
  /**
    * Applies a function A => B to the first element of the pair, preserving
    * only the second element, which is the move type.
    * @return A pair consisting of the result of the function application as the
    *         first element, and the move type as the second element.
    */
  def withMoveType[A, B](pair: (A, MoveType))(f: A => B) = pair match {
    case (value, moveType) => (f(value), moveType)
  }

  def moveGenerator: PieceType => BitboardMoveGenerator = {
    case Pawn => PawnMoveGenerator
    case Knight => KnightMoveGenerator
    case Bishop => BishopMoveGenerator
    case Rook => RookMoveGenerator
    case Queen => QueenMoveGenerator
    case King => KingMoveGenerator
  }
}

