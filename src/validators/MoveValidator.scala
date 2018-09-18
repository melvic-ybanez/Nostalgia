package validators

import engine.board._
import engine.movegen._
import engine.movegen.Location._
import engine.movegen.Move.LocationMove

/**
  * Created by melvic on 9/14/18.
  */
object MoveValidator {
  type MoveValidation = LocationMove => Board => Option[MoveType]

  def validateMove: MoveValidation = move => board => board(move.source) map {
      case Piece(Pawn, side) => validatePawnMove(side)
    } map (_(move)(board)) getOrElse None

  def validatePawnMove(side: Side): MoveValidation = move => board => {
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

    def validateCapture(side: Side, direction: Int) = board(move.destination).flatMap {
      case Piece(_, destSide) if destSide == side.opposite =>
        handlePromotion(side, Normal)
    } orElse validateEnPassant(side, direction)

    def validateEnPassant(side: Side, direction: Int) = {
      val sideLocation: Location = Location(move.source.file + direction, move.source.rank)
      board(sideLocation).flatMap { _ =>
        board.lastMove match {
          case Some(Move(_, lastDest, DoublePawnPush)) if lastDest == sideLocation => Some(EnPassant)
          case _ => None
        }
      }
    }

    def handlePromotion(side: Side, moveType: MoveType) = Some {
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
      case ((fd, 1), White) if Math.abs(fd) == 1 => validateCapture(White, fd)
      case ((fd, -1), Black) if Math.abs(fd) == 1 => validateCapture(Black, fd)
      case _ => None
    }
  }

  def delta(move: LocationMove): (Int, Int) = {
    def delta(locOp: Location => Int): Int = locOp(move.destination) - locOp(move.source)
    (delta(_.file), delta(_.rank))
  }
}

