package scala.engine.movegen.scala

import scala.engine.board.bitboards.Bitboard.U64
import BitboardMoveGenerator._
import scala.engine.board.Side
import scala.engine.board.bitboards.Bitboard
import scala.engine.board.scala.Side

/**
  * Created by melvic on 8/5/18.
  */
trait BitboardMoveGenerator {
  type Generator[A] = (Bitboard, Int, Side) => A
  type GeneratorStream[A] = Generator[Stream[A]]
  type WithMoveType[A] = (A, MoveType)

  def destinationBitsets: GeneratorStream[WithMoveType[U64]]

  def validDestinationBitsets: GeneratorStream[WithMoveType[U64]] = destinationBitsets(_, _, _)
    .filter { case (board, _) => Bitboard.isNonEmptySet(board) }

  def attackBitsets: GeneratorStream[U64] = validDestinationBitsets(_, _, _).filter {
    case (_, Attack) => true
    case _ => false
  } map { case (bitboard, _) => bitboard }

  def destinations: GeneratorStream[WithMoveType[Int]] = validDestinationBitsets(_, _, _)
    .map(withMoveType(Bitboard.oneBitIndex))

  def emptyOrOpponent(emptySquares: U64, opponents: U64): U64 => U64 = _ & (emptySquares | opponents)
}

object BitboardMoveGenerator {
  def withMoveType[A, B](f: A => B)(pair: (A, MoveType)) = pair match {
    case (value, moveType) => (f(value), moveType)
  }
}

