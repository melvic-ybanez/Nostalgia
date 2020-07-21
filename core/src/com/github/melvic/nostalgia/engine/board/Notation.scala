package com.github.melvic.nostalgia.engine.board

import com.github.melvic.nostalgia.engine.api.movegen.{File, Rank}
import com.github.melvic.nostalgia.engine.api.piece.PieceType._
import com.github.melvic.nostalgia.engine.base.MoveType.{Castling, EnPassant, PawnPromotion}
import com.github.melvic.nostalgia.engine.base.{Board, Move, MoveType, Piece, Side, Square}
import com.github.melvic.nostalgia.engine.base.implicits._
import com.github.melvic.nostalgia.engine.movegen._
import com.github.melvic.nostalgia.validators.MoveValidator
import cats.implicits._

/**
  * Created by melvic on 1/23/19.
  */
trait Notation[T, S, L] {
  type NBoard = Board[T, S, L]
  type NMove = Move[T, S, L]

  /**
   * Proof that L is an instance of Square
   */
  implicit def square: Square[L]

  /**
   * Proof that S is an instance of Side
   */
  implicit def side: Side[S]

  def ofPieceType(pieceType: T): Option[String] = pieceType match {
    case Pawn => None
    case Knight => "N".some
    case Bishop => "B".some
    case Rook => "R".some
    case Queen => "Q".some
    case King => "K".some
  }

  def ofSquare(location: L): String =
    ofFile(Square[L].file(location)) + ofRank(Square[L].rank(location))

  def ofFile(file: Square[L]#File) = file.toString.toLowerCase
  def ofRank(rank: Square[L]#Rank) = rank.toString.tail

  def ofCapture(board: NBoard, move: Move[T, S, L]) = {
    lazy val captureNotation = Some("x")
    move match {
      case Move(_, _, EnPassant) => captureNotation
      case Move(_, destination, _) =>
        if (board(destination).isDefined) captureNotation
        else None
      case _ => None
    }
  }

  def ofPawnSuffix: MoveType[T, S] => Option[String] = {
    case EnPassant => Some("e.p.")
    case PawnPromotion(Piece(pieceType, _)) => ofPieceType(pieceType)
    case _ => None
  }

  def ofCheckedSuffix(board: NBoard, side: S) =
    if (board.isChecked(side)) Some("+") else None

  def ofCheckMateSuffix(board: NBoard, side: S) =
    if (board.isCheckmate(side)) Some("#") else None

  def ofCastling: NMove => Option[String] = {
    case Move(_, square, Castling) if Square[L].file(square) => Some("O-O-O")
    case Move(_, _, Castling) => Some("O-O")
    case _ => None
  }

  def ofDisambiguation(move: NMove, piece: Piece[T, S], board: NBoard) = move match {
    case Move(source, destination, moveType) =>
      val sourceFile = Square[L].file(source)
      val sourceRank = Square[L].rank(source)

      // Get the locations of the pieces that can move to the
      // destination square.
      val locations = board.pieceLocations(piece).filter { location =>
        val _move = Move(location, destination, moveType)
        MoveValidator.validateMove(_move)(board).isDefined
      }

      // If only one piece can do the move, there is no need to disambiguate.
      if (locations.size == 1) None
      else Some {
        if (locations.count(_.file == sourceFile) == 1) ofFile(sourceFile)
        else if (locations.count(_.rank == sourceRank) == 1) ofRank(sourceRank)
        else ofSquare(source)
      }
  }

  /**
    * Generates the Algebraic Notation of a given move.
    *
    * TODO: draw offers, end of game
    * @param move The move, containing the source, destination, and move type.
    * @param piece The moving piece.
    * @param board The current board position
    * @return The string representation of the move's algebraic notation.
    */
  def ofMove(move: NMove, piece: Piece[T, S], board: NBoard): String = move match {
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
          .flatMap(combineWith(Some(ofSquare(destination))))

          // Append the suffix, if there is one.
          .flatMap(combineWith(ofPawnSuffix(moveType)))

          // If it's checkmate, append the checkmate notation.
          // If the opposite side is in checked, append the checked notation.
          // Otherwise, return the current result.
          .flatMap { notation =>
            val side = piece.side
            val updatedBoard = board.updateByMove(move, piece)

            ofCheckMateSuffix(updatedBoard, piece.side)
              .map(notation + _)
              .orElse(combineWith(ofCheckedSuffix(updatedBoard, Side[S].opposite(side)))(notation))
          }
      }.get
  }
}
