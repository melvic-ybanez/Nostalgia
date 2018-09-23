package validators

import engine.board._
import engine.movegen._
import engine.movegen.Location._
import engine.movegen.Move.LocationMove

import scala.annotation.tailrec

/**
  * Created by melvic on 9/14/18.
  */
object MoveValidator {
  type MoveValidation = LocationMove => Board => Option[MoveType]
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
      case _ => None
    }

  def validateSlidingMove(side: Side)
      (notAllowed: (Int, Int) => Boolean)
      (step: Int => Int): MoveValidation = { case move @ Move(source, destination, _) => board =>
    val (fileDelta, rankDelta) = delta(move)
    if (notAllowed(Math.abs(fileDelta), Math.abs(rankDelta))) None
    else {
      val fileStep = step(fileDelta)
      val rankStep = step(rankDelta)

      @tailrec
      def recurse(file: File, rank: Rank): Option[MoveType] =
        if (file == destination.file && rank == destination.rank)
          captureOrEmpty(side, destination, board)(() => Some(Normal))
        else if (board(file, rank).isDefined) None
        else recurse(file + fileStep, rank + rankStep)

      recurse(source.file + fileStep, source.rank + rankStep)
    }
  }

  def validateCapture(side: Side, location: Location, board: Board)(implicit  f: () => Option[MoveType]) =
    board(location) flatMap {
      case Piece(_, destSide) if destSide == side.opposite => f()
      case _ => None
    }

  def isChecked: PieceMoveValidation = { case piece @ Piece(_, side) => move => board =>
    def checkAttacker(attacker: Piece): MoveType => Option[MoveType] =
      _ => board(move.destination) flatMap { p => if (p == attacker) Some(Normal) else None }

    val attackFunctions = List(
      (validateKnightMove, Knight),
      (validateBishopMove, Bishop),
      (validateRookMove, Rook),
      (validateQueenMove, Queen))

    attackFunctions.foldLeft[Option[MoveType]](None) { case (result, (f, attackerType)) =>
      result.orElse(f(piece)(move)(board).flatMap(checkAttacker(Piece(attackerType, side.opposite))))
    }
  }

  def captureOrEmpty(side: Side, location: Location, board: Board)(implicit f: () => Option[MoveType]) =
    validateCapture(side, location, board) orElse f()

  def delta(move: LocationMove, abs: Boolean = false): (Int, Int) = {
    def delta(locOp: Location => Int): Int = {
      val _delta = locOp(move.destination) - locOp(move.source)
      if (abs) Math.abs(_delta) else _delta
    }
    (delta(_.file), delta(_.rank))
  }
}

