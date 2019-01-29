package engine.board

import engine.movegen._
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

  // TODO: enPassant, long castling, promotion, ambiguous sources
  def of(move: LocationMove, piece: Piece, board: Board): String = move match {
    case Move(_, _, Castling) => "O-O"
    case Move(_, destination, _) =>
      val pieceTypeNotation = Notation.of(piece.pieceType)
      val moveNotation = Notation.of(destination)
      val captureString = board(destination) map (_ => "x") getOrElse ""
      val newBoard = board.updateByMove(move, piece)
      val checkString =
        if (newBoard.isCheckmate(piece.side)) "#"
        else if (newBoard.isChecked(piece.side.opposite)) "+"
        else ""
      s"$pieceTypeNotation$captureString$moveNotation$checkString"
  }
}
