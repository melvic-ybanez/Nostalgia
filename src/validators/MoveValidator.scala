package validators

import engine.board._
import engine.movegen.{Location, Move}
import engine.movegen.Location._
import engine.movegen.Move.LocationMove

/**
  * Created by melvic on 9/14/18.
  */
object MoveValidator {
  type MoveValidator = LocationMove => Board => Option[String]

  def error(message: String) = Some(s"Invalid move: $message.")

  def findPiece(pieceType: PieceType)(f: Side => MoveValidator): MoveValidator =
    move => board => board(move.source) match {
      case Some(Piece(pieceType0, side)) if pieceType0 == pieceType => f(side)(move)(board)
      case _ => Some(s"$pieceType not found")
    }

  def validatePawnMove: MoveValidator = findPiece(Pawn) { side => move => board =>
    lazy val direction = if (side == White) 1 else -1
    lazy val invalidDirection = error("Invalid pawn direction.")

    (delta(move), side) match {
      case ((_, 0), _) => error(s"Not pushing")
      case ((_, rankDelta), Black) if rankDelta > 0 => invalidDirection
      case ((_, rankDelta), White) if rankDelta < 0 => invalidDirection
      case ((_, rankDelta), _) if rankDelta > 2 => error("Pushing more than 2 steps")
      case ((fileDelta, _), _) if fileDelta > 1 => error("Moving to the side with more than a step")
      case (())
    }
  }

  def delta(move: LocationMove): (Int, Int) = {
    def delta(locOp: Location => Int): Int = locOp(move.destination) - locOp(move.source)
    (delta(_.file), delta(_.rank))
  }
}

