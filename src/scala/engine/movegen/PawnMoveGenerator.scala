package scala.engine.movegen

import java.engine.board.Board

import scala.engine.board.bitboards.Bitboard._
import scala.engine.board.bitboards.Implicits.Piece._
import BitboardMoveGenerator._
import engine.board.scala.{Side, White}
import scala.engine.board.{Side, White}
import scala.engine.board.bitboards.Bitboard

/**
  * Created by melvic on 8/5/18.
  */
object PawnMoveGenerator extends BitboardMoveGenerator with PostShiftOneStep {
  type PawnMove = (U64, U64, Side) => U64

  def singlePush: PawnMove = (pawns, emptySquares, sideToMove) => {
    // Move north first,
    val pushedNorth = north(pawns)

    // then south twice, if piece is black
    val optionalTwoRows = sideToMove << 4
    val board = pushedNorth >> optionalTwoRows

    // And make sure you land on an empty square
    board & emptySquares
  }

  def doublePush: PawnMove = (pawns, emptySquares, sideToMove) => {
    val destinationMasks = List(
      0x00000000FF000000L,  // rank 4 (white's double push destination)
      0x000000FF00000000L   // rank 5 (black's double push destination)
    )

    val pushedPawns = singlePush(pawns, emptySquares, sideToMove)
    singlePush(pushedPawns, emptySquares, sideToMove) & destinationMasks(sideToMove)
  }

  def attack(northAttack: U64 => U64, southAttack: U64 => U64): PawnMove = {
    case (pawns, opponents, sideToMove) =>
      val move = sideToMove match {
        case White => northAttack
        case _ => southAttack
      }
      move(pawns) & opponents
  }

  def attackEast: PawnMove = attack(northEast, southEast)
  def attackWest: PawnMove = attack(northWest, southWest)

  def enPassant: PawnMove = {
    case (pawns, opponent, sideToMove) =>
      // Decide whether to move north or south based on the color
      val (setwiseOp, action): (SetwiseOperator, U64 => U64) = sideToMove match {
        case White => (_ << _, north)
        case _ => (_ >> _, south)
      }

      val newOpponent = setwiseOp(pawns, Board.SIZE)

      // Decide whether to move east or west for the attack
      val attack = {
        val pawnPosition = Bitboard.oneBitIndex(pawns)
        val opponentPosition = Bitboard.oneBitIndex(opponent)
        action andThen (
          if (pawnPosition % Board.SIZE > opponentPosition % Board.SIZE) west
          else east
        )
      }

      attack(pawns) & newOpponent
  }

  def generatePawnMoves(moves: Stream[WithMoveType[PawnMove]],
                        getTargets: (Bitboard, Side) => U64): GeneratorStream[WithMoveType[U64]] = {
    case (bitboard, source, sideToMove) =>
      val pawns = Bitboard.singleBitset(source)
      moves.map(withMoveType(_(pawns, getTargets(bitboard, sideToMove), sideToMove)))
  }

  def pushMoves = Stream((singlePush, Normal), (doublePush, DoublePawnPush))
  def attackMoves = Stream((attackEast, Attack), (attackWest, Attack), (enPassant, EnPassant))

  def generatePushes = generatePawnMoves(pushMoves, (board, side) => board.emptySquares)
  def generateAttacks = generatePawnMoves(attackMoves, (board, side) => board.opponents(side))

  def destinationBitsets: GeneratorStream[WithMoveType[U64]] = (bitboard, source, sideToMove) =>
    generatePushes(bitboard, source, sideToMove) ++ generateAttacks(bitboard, source, sideToMove)
}
