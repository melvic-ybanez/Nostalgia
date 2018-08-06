package scala.engine.board

/**
  * Created by melvic on 8/6/18.
  */
sealed trait PieceType
case object Pawn extends PieceType
case object Knight extends PieceType
case object Bishop extends PieceType
case object Rook extends PieceType
case object Queen extends PieceType
case object King extends PieceType

sealed trait Side {
  def opposite: Side = this match {
    case White => Black
    case Black => White
  }
}

case object White extends Side
case object Black extends Side

case class Piece(pieceType: PieceType, side: Side)

object Piece {
  def whiteOf(pieceType: PieceType) = Piece(pieceType, White)
  def blackOf(pieceType: PieceType) = Piece(pieceType, Black)
}
