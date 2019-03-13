package ybanez.nostalgia.engine.movegen.bitboards

import ybanez.nostalgia.engine.board.Piece._
import ybanez.nostalgia.engine.board.bitboards.Bitboard
import ybanez.nostalgia.engine.board.bitboards.Bitboard._
import ybanez.nostalgia.engine.board._
import ybanez.nostalgia.engine.movegen._
import ybanez.nostalgia.engine.movegen.bitboards.BitboardMoveGenerator._

/**
  * Created by melvic on 8/5/18.
  */
object PawnMoveGenerator extends BitboardMoveGenerator with PostShiftOneStep {
  type PawnMove = (U64, U64, Side) => U64

  def singlePush: PawnMove = { (pawns, emptySquares, sideToMove) =>
    // Move north first,
    val pushedNorth = north(pawns)

    // then south twice, if piece is black
    val optionalTwoRows = sideToMove << 4
    val board = pushedNorth >> optionalTwoRows

    // And make sure you land on an empty square
    board & emptySquares
  }

  def doublePush: PawnMove = { (pawns, emptySquares, sideToMove) =>
    val destinationMasks = List(
      0x00000000FF000000L,  // rank 4 (white's double push destination)
      0x000000FF00000000L   // rank 5 (black's double push destination)
    )

    val pushedPawns = singlePush(pawns, emptySquares, sideToMove)
    singlePush(pushedPawns, emptySquares, sideToMove) & destinationMasks(sideToMove)
  }

  def attack(northAttack: U64 => U64, southAttack: U64 => U64): PawnMove = {
    (pawns, opponents, sideToMove) =>
      val move = sideToMove match {
        case White => northAttack
        case _ => southAttack
      }
      move(pawns) & opponents
  }

  def attackEast: PawnMove = attack(northEast, southEast)
  def attackWest: PawnMove = attack(northWest, southWest)

  def enPassant(enPassantBitset: U64): PawnMove = { (pawnBitset, opponent, sideToMove) =>
    // Decide whether to go east or west if en-passant is possible
    val fileOperationOpt =
      if (isNonEmptySet(enPassantBitset & east(pawnBitset))) Some(east)
      else if (isNonEmptySet(enPassantBitset & west(pawnBitset))) Some(west)
      else None

    fileOperationOpt.map { fileOperation =>
      // Decide whether to move north or south based on the color
      val rankOperation: U64 => U64 = sideToMove match {
        case White => north
        case _ => south
      }

      (rankOperation andThen fileOperation)(pawnBitset)
    }.getOrElse(0L)
  }

  /**
    * @param getTargets Determines the target square (empty, occupied by opponents, etc.).
    * @return A move generator of stream of (U64, MoveType) denoting the pawn moves.
    */
  def generatePawnMoves(moves: Stream[WithMove[PawnMove]],
                        getTargets: (Bitboard, Side) => U64): StreamGen[WithMove[U64]] = {
    case (bitboard, source, sideToMove) =>
      val pawnBitset = Bitboard.singleBitset(source)
      moves flatMap { case move@(pawnMove, _) =>
        val moveBitset: PawnMove => U64 = _(pawnBitset, getTargets(bitboard, sideToMove), sideToMove)

        val promotionMasks = 0x00ff000000000000L :: 0xff00L :: Nil
        val promote = isNonEmptySet(promotionMasks(sideToMove) & pawnBitset)

        if (promote)
          // Generate bitboards for each position promotions
          Stream(Knight, Bishop, Rook, Queen) map { officerType =>
            val promotionOptions = (pawnMove, PawnPromotion(Piece(officerType, sideToMove)))
            withMoveType(promotionOptions)(moveBitset)
          }
        else Stream(withMoveType(move)(moveBitset))
      }
  }

  def pushMoves = Stream((singlePush, Normal), (doublePush, DoublePawnPush))
  def attackMoves = Stream((attackEast, Attack), (attackWest, Attack))
  def enPassantMove(enPassantBitset: U64) = Stream((enPassant(enPassantBitset), EnPassant))

  def generatePushes = generatePawnMoves(pushMoves, (board, _) => board.emptySquares)
  def generateAttacks = generatePawnMoves(attackMoves, (board, side) => board.opponents(side))
  def generateEnPassant(enPassantBitset: U64) = generatePawnMoves(
    enPassantMove(enPassantBitset), (board, _) => board.emptySquares)

  def destinationBitsets: StreamGen[WithMove[U64]] = (bitboard, source, sideToMove) =>
    generatePushes(bitboard, source, sideToMove) ++
      generateAttacks(bitboard, source, sideToMove) ++
      generateEnPassant(bitboard.enPassantBitset)(bitboard, source, sideToMove)
}
