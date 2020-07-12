package com.github.melvic.nostalgia.engine.movegen.bitboards

import com.github.melvic.nostalgia.engine.base.MoveType.Attack
import com.github.melvic.nostalgia.engine.board.bitboards._
import com.github.melvic.nostalgia.engine.movegen.bitboards.BitboardMoveGenerator.withMoveType

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
    * @tparam A the type of the elements in the generated stream.
    */
  type ListGen[A] = Generator[List[A]]

  /**
    * A pair comprised of a value of type A and a particular move type.
    */
  type WithMove[A] = (A, MoveType)

  def destinationBitsets: ListGen[WithMove[U64]]

  def nonEmptyDestinationBitsets: ListGen[WithMove[U64]] = destinationBitsets(_, _, _)
    .filter { case (bitboard, _) => Bitboard.isNonEmptySet(bitboard) }

  def attackBitsets: ListGen[U64] = nonEmptyDestinationBitsets(_, _, _).filter {
    case (_, Attack) => true
    case _ => false
  } map { case (bitboard, _) => bitboard }

  def destinations: ListGen[WithMove[Int]] = nonEmptyDestinationBitsets(_, _, _)
    .map(withMoveType(_)(Bitboard.bitScan))

  def validMoves: ListGen[Move] = { (bitboard, source, side) =>
    destinations(bitboard, source, side).map { case (destination, moveType) =>
      Move(source, destination, moveType)
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

