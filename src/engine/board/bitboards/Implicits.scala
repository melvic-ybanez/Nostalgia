package engine.board.bitboards

import engine.board._
import engine.board._
import engine.movegen._

/**
  * Created by melvic on 8/5/18.
  */
object Implicits {
  object Piece {
    lazy val pieceTypes = List(Pawn, Knight, Bishop, Rook, Queen, King)

    implicit def pieceTypeToInt(pieceType: PieceType): Int =
      pieceTypes.indexOf(pieceType) + Bitboard.PieceTypeOffset

    implicit def intToPieceType(i: Int): PieceType = pieceTypes(i)

    implicit def pieceSideToInt(side: Side): Int = side match {
      case White => 0
      case Black => 1
    }

    implicit def intToSide(i: Int): Side = (White :: Black :: Nil)(i)
  }

  object Location {
    implicit def fileToInt(file: File): Int = List(A, B, C, D, E, F, G, H).indexOf(file)

    implicit def rankToInt(rank: Rank): Int = List(_1, _2, _3, _4, _5, _6, _7, _8).indexOf(rank)

    implicit def locationToInt(location: Location): Int = location.rank * Board.Size + location.file
  }
}
