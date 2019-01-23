package engine.board

import engine.movegen.{Location, Move}
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

  def of(move: LocationMove, board: Board, checkmate: Boolean): String = move match {
    case Move(source, destination, moveType) => board(source) map { movingPiece =>
      val captureString = board(destination).map(_ => "x") getOrElse ""
      val checkmateString = if (checkmate) "#" else ""
      val pieceTypeNotation = Notation.of(movingPiece.pieceType)
      val moveNotation = Notation.of(destination)

      s"$pieceTypeNotation$captureString$moveNotation$checkmateString"
    } getOrElse ""
  }
}
