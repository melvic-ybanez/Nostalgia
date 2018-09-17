package validators

import engine.board._
import engine.movegen.{Location, Move}
import engine.movegen.Location._
import engine.movegen.Move.LocationMove

/**
  * Created by melvic on 9/14/18.
  */
object MoveValidator {
  type MoveValidation = LocationMove => Board => Boolean

  def validateMove: MoveValidation =
    move => board => board(move.source) map {
      case Piece(Pawn, side) => validatePawnMove(side)
    } exists (_(move)(board))

  def validatePawnMove(side: Side): MoveValidation = move => board => {
    def validatePush(step: Int) =
      board(move.destination.file, move.source.rank + step).isEmpty

    (delta(move), side) match {
      case ((0, 1), White) => validatePush(1)
      case ((0, 2), White) => validatePush(1) && validatePush(2)
      case ((0, -1), Black) => validatePush(-1)
      case ((0, -2), Black) => validatePush(-1) && validatePush(-2)
      case _ => false
    }
  }

  def delta(move: LocationMove): (Int, Int) = {
    def delta(locOp: Location => Int): Int = locOp(move.destination) - locOp(move.source)
    (delta(_.file), delta(_.rank))
  }
}

