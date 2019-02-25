package engine.board

import engine.movegen._
import engine.movegen.Move.LocationMove

/**
  * Created by melvic on 1/23/19.
  */
object Notation {
  def ofPieceType(pieceType: PieceType) = pieceType match {
    case Pawn => ""
    case Knight => "N"
    case Bishop => "B"
    case Rook => "R"
    case Queen => "Q"
    case King => "K"
  }

  def ofLocation(location: Location): String = {
    val fileNotation = ofFile(location.file)
    val rankNotation = location.rank.toString.tail
    fileNotation + rankNotation
  }

  def ofFile(file: File) = file.toString.toLowerCase

  // TODO: enPassant, promotion, ambiguous sources
  def ofMove(move: LocationMove, piece: Piece, board: Board): String = move match {
    case Move(_, destination, Castling) =>
      if (destination.file == C) "O-O-O"    // Queen-side castling
      else "O-O"    // King-side castling
    case Move(_, destination, moveType) =>
      val isEnPassant = moveType == EnPassant

      // If the destination is not empty, it is assumed to be a capture move.
      val capture = board(destination).isDefined || isEnPassant

      val pieceTypeNotation =
        if (piece.pieceType == Pawn && capture) ofFile(move.source.file)
        else Notation.ofPieceType(piece.pieceType)

      val captureString = if (capture) "x" else ""
      val moveNotation = Notation.ofLocation(destination)
      val enPassantSuffix = if (isEnPassant) "e.p." else ""

      val newBoard = board.updateByMove(move, piece)
      val checkString =
        if (newBoard.isCheckmate(piece.side)) "#"
        else if (newBoard.isChecked(piece.side.opposite)) "+"
        else ""

      s"$pieceTypeNotation$captureString$moveNotation$enPassantSuffix$checkString"
  }
}
