package engine.board

import engine.movegen.Location
import engine.movegen.Move.LocationMove

/**
  * Created by melvic on 1/23/19.
  */
object Notation {
  def of(pieceType: PieceType) = pieceType match {
    case Pawn => ""
    case Knight => "N"
    case Bishop => "B"
    case Rook => "R"
    case Queen => "Q"
    case King => "K"
  }

  def of(location: Location): String = {
    val fileNotation = location.file.toString.toLowerCase
    val rankNotation = location.rank.toString.tail
    fileNotation + rankNotation
  }
}

case class Notation(piece: Piece, move: LocationMove) {
  override def toString = {
    val pieceTypeNotation = Notation.of(piece.pieceType)
    val moveNotation = Notation.of(move.destination)
    pieceTypeNotation + moveNotation
  }
}
