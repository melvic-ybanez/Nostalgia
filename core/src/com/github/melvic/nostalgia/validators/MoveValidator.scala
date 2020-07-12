package com.github.melvic.nostalgia.validators

import com.github.melvic.nostalgia.engine.base
import com.github.melvic.nostalgia.engine.board._
import com.github.melvic.nostalgia.engine.movegen.Location._
import com.github.melvic.nostalgia.engine.movegen.Move.LocationMove
import com.github.melvic.nostalgia.engine.movegen._

import scala.annotation.tailrec

/**
  * Created by melvic on 9/14/18.
  */
object MoveValidator {
  type MoveValidation = LocationMove => Board => Option[base.MoveType]
  type PieceMoveValidation = Piece => MoveValidation

  def validateMove: MoveValidation = move => board => board(move.source) flatMap { piece =>
      val validator: PieceMoveValidation = piece match {
        case Piece(Pawn, _) => validatePawnMove
        case Piece(Knight, _) => validateKnightMove
        case Piece(Bishop, _) => validateBishopMove
        case Piece(Rook, _) => validateRookMove
        case Piece(Queen, _) => validateQueenMove
        case Piece(King, _) => validateKingMove
      }
      validator(piece)(move)(board)
    }

  def validatePawnMove: PieceMoveValidation = { case Piece(_, side) => move => board =>
    def validateSinglePush(direction: Int) =
      board(move.destination.file, move.source.rank + direction) match {
        case Some(_) => None
        case None => handlePromotion(side, Normal)
      }

    def validateDoublePush(step: Int, side: Side) =
      validateSinglePush(step).flatMap { _ =>
        validateSinglePush(step * 2)
      } flatMap { _ =>
        val expectedRank = side match {
          case White => _2
          case Black => _7
        }
        if (expectedRank == move.source.rank) Some(DoublePawnPush)
        else None
      }

    def validatePawnCapture(side: Side, direction: Int) =
      MoveValidator.validateCapture(side, move.destination, board)(() => handlePromotion(side, Attack))
        .orElse(validateEnPassant(side, direction))

    def validateEnPassant(side: Side, direction: Int) = {
      val sideLocation: Location = Location(move.source.file + direction, move.source.rank)
      board(sideLocation).flatMap { _ =>
        board.lastMove match {
          case Some(Move(_, lastDest, DoublePawnPush)) if lastDest == sideLocation => Some(EnPassant)
          case _ => None
        }
      }
    }

    def handlePromotion(side: Side, moveType: base.MoveType) = Some {
      (side, move) match {
        case (White, Move(_, Location(_, _8), promotion: PawnPromotion)) => promotion
        case (Black, Move(_, Location(_, _1), promotion: PawnPromotion)) => promotion
        case _ => moveType
      }
    }

    (delta(move), side) match {
      case ((0, 1), White) => validateSinglePush(1)
      case ((0, 2), White) => validateDoublePush(1, White)
      case ((0, -1), Black) => validateSinglePush(-1)
      case ((0, -2), Black) => validateDoublePush(-1, Black)
      case ((fd, 1), White) if Math.abs(fd) == 1 => validatePawnCapture(White, fd)
      case ((fd, -1), Black) if Math.abs(fd) == 1 => validatePawnCapture(Black, fd)
      case _ => None
    }
  }

  def validateKnightMove: PieceMoveValidation = { case Piece(_, side) => move => board =>
    implicit val optMoveType = () => Some(Normal)

    delta(move, abs = true) match {
      case (1, 2) => captureOrEmpty(side, move.destination, board)
      case (2, 1) => captureOrEmpty(side, move.destination, board)
      case _ => None
    }
  }

  def validateBishopMove: PieceMoveValidation = piece =>
    validateSlidingMove(piece.side)(_ != _)(delta => if (delta > 0) 1 else -1)

  def validateRookMove: PieceMoveValidation = piece =>
    validateSlidingMove(piece.side)(_ != 0 && _ != 0) { delta =>
      if (delta > 0) 1 else if (delta < 0) -1 else 0
    }

  def validateQueenMove: PieceMoveValidation = piece => move => board =>
        validateBishopMove(piece)(move)(board) orElse validateRookMove(piece)(move)(board)

  def validateKingMove: PieceMoveValidation = piece => move => board =>
    delta(move, abs = true) match {
      case (1, 1) | (1, 0) | (0, 1) =>
        captureOrEmpty(piece.side, move.destination, board)(() => Some(Normal))

      // handle castling case
      case (2, 0) if !board.canCastle(move) => None
      case (2, 0) if board.isChecked(piece.side) => None
      case (2, 0) =>
        val direction = move.destination.file - move.source.file

        // move one step closer to the destination
        val singleStep = if (direction > 0) 1 else -1
        val singleStepFile: File = move.source.file + singleStep
        val singleStepLocation = Location(singleStepFile, move.destination.rank)

        board.at(singleStepLocation)
          .flatMap(_ => None)   // the next square is occupied; abort
          .orElse {
            val singleStepMove = Move[Location](
              move.source, Location(singleStepFile, move.destination.rank))
            val singleStepBoard = board.updateByMove(singleStepMove, piece)

            if (singleStepBoard.isChecked(piece.side)) None
            else board.at(move.destination)
              .flatMap(_ => None)   // destination is occupied; abort
              .orElse(Some(Castling))
          }

      case _ => None
    }

  def validateSlidingMove(side: Side)
      (notAllowed: (Int, Int) => Boolean)
      (step: Int => Int): MoveValidation = {
    case move @ Move(source, destination, _) => board =>
      val (fileDelta, rankDelta) = delta(move)
      if (notAllowed(Math.abs(fileDelta), Math.abs(rankDelta))) None
      else {
        val fileStep = step(fileDelta)
        val rankStep = step(rankDelta)

        @tailrec
        def recurse(file: File, rank: Rank): Option[base.MoveType] =
          if (file == destination.file && rank == destination.rank)
            captureOrEmpty(side, destination, board)(() => Some(Normal))
          else if (board(file, rank).isDefined) None
          else recurse(file + fileStep, rank + rankStep)

        recurse(source.file + fileStep, source.rank + rankStep)
      }
  }

  def validateCapture(side: Side, destination: Location, board: Board)
      (implicit  f: () => Option[base.MoveType]) =
    board(destination) flatMap {
      case Piece(_, destSide) if destSide == side.opposite => f()
      case _ => None
    }

  def captureOrEmpty(side: Side, destination: Location, board: Board)
      (implicit f: () => Option[base.MoveType]) =
    validateCapture(side, destination, board).orElse {
      board(destination).flatMap(_ => None).orElse(f())
    }

  /**
    * Computes the distance between the source and destination of a move.
    * @param move the given move
    * @param abs whether the distance should be absoluate or not
    * @return A pair of integers representing the file and rank distances
    */
  def delta(move: LocationMove, abs: Boolean = false): (Int, Int) = {
    def delta(locOp: Location => Int): Int = {
      val _delta = locOp(move.destination) - locOp(move.source)
      if (abs) Math.abs(_delta) else _delta
    }
    (delta(_.file), delta(_.rank))
  }
}

