package engine.board

import engine.movegen._
import engine.movegen.Move.LocationMove
import validators.MoveValidator

/**
  * Created by melvic on 1/23/19.
  */
object Notation {
  def ofPieceType(pieceType: PieceType): Option[String] = pieceType match {
    case Pawn => None
    case _ => Some { pieceType match {
      case Knight => "N"
      case Bishop => "B"
      case Rook => "R"
      case Queen => "Q"
      case King => "K"
    }}
  }

  def ofLocation(location: Location): String =
    ofFile(location.file) + ofRank(location.rank)

  def ofFile(file: File) = file.toString.toLowerCase
  def ofRank(rank: Rank) = rank.toString.tail

  def ofCapture(board: Board, move: LocationMove) = {
    lazy val captureNotation = Some("x")
    move match {
      case Move(_, _, EnPassant) => captureNotation
      case Move(_, destination, _) =>
        if (board(destination).isDefined) captureNotation
        else None
      case _ => None
    }
  }

  def ofSuffix: MoveType => Option[String] = {
    case EnPassant => Some("e.p.")
    case PawnPromotion(Piece(pieceType, _)) => ofPieceType(pieceType)
    case _ => None
  }

  def ofCheckedSuffix(board: Board, side: Side) =
    if (board.isChecked(side)) Some("+") else None

  def ofCheckMateSuffix(board: Board, side: Side) =
    if (board.isCheckmate(side)) Some("#") else None

  def ofCastling: LocationMove => Option[String] = {
    case Move(_, Location(C, _), Castling) => Some("O-O-O")
    case Move(_, _, Castling) => Some("O-O")
    case _ => None
  }

  def ofDisambiguation(move: LocationMove, piece: Piece, board: Board) = move match {
    case Move(source@Location(sourceFile, sourceRank), destination, moveType) =>
      // Get the locations of the pieces that can move to the
      // destination square.
      val locations = board.pieceLocations(piece).filter { location =>
        val _move = Move[Location](location, destination, moveType)
        MoveValidator.validateMove(_move)(board).isDefined
      }

      // If only one piece can do the move, there is no need to disambiguate.
      if (locations.size == 1) None
      else Some {
        if (locations.count(_.file == sourceFile) == 1) ofFile(sourceFile)
        else if (locations.count(_.rank == sourceRank) == 1) ofRank(sourceRank)
        else ofLocation(source)
      }
  }

  /**
    * Generates the Algebraic Notation of a given move.
    *
    * TODO: ambiguous sources, draw offers, end of game
    * @param move The move, containing the source, destination, and move type.
    * @param piece The moving piece.
    * @param board The current board position
    * @return The string representation of the move's algebraic notation.
    */
  def ofMove(move: LocationMove, piece: Piece, board: Board): String = move match {
    case Move(source, destination, moveType) =>
      lazy val captureNotation = ofCapture(board, move)

      def combineWith(nextNotationOpt: Option[String])(notation: String) =
        nextNotationOpt.map(notation + _).orElse(Some(notation))

      ofCastling(move)
        .orElse {
          // If it's not a castling move, start with the piece type notation.
          ofPieceType(piece.pieceType)

          // Append disambiguation notation.
          .flatMap(combineWith(ofDisambiguation(move, piece, board)))

          // Then combine the result with the capture notation,
          .flatMap(combineWith(captureNotation))

          // unless there isn't a type notation, in which case the moving
          // piece must be a pawn, and therefore we should use the source
          // file for the piece type notation.
          .orElse(captureNotation.map(ofFile(source.file) + _).orElse(Some("")))

          // Append the destination notation.
          .flatMap(combineWith(Some(ofLocation(destination))))

          // Append the suffix, if there is one.
          .flatMap(combineWith(ofSuffix(moveType)))

          // If it's checkmate, append the checkmate notation.
          // If the opposite side is in checked, append the checked notation.
          // Otherwise, return the current result.
          .flatMap { notation =>
            val side = piece.side
            val updatedBoard = board.updateByMove(move, piece)

            ofCheckMateSuffix(updatedBoard, piece.side)
              .map(notation + _)
              .orElse(combineWith(ofCheckedSuffix(updatedBoard, side.opposite))(notation))
          }
      }.get
  }
}
