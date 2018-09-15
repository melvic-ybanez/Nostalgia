package engine.utils

import engine.board._
import engine.board.bitboards.Bitboard
import engine.movegen._

/**
  * Created by melvic on 8/5/18.
  */
object Implicits {
  object Pieces {
    lazy val pieceTypes = List(Pawn, Knight, Bishop, Rook, Queen, King)

    // TODO: Make this less dependent on a constant from the Bitboard class
    implicit def pieceTypeToInt(pieceType: PieceType): Int =
      pieceTypes.indexOf(pieceType) + Bitboard.PieceTypeOffset

    implicit def intToPieceType(i: Int): PieceType = pieceTypes(i)

    implicit def pieceSideToInt(side: Side): Int = side match {
      case White => 0
      case Black => 1
    }

    implicit def intToSide(i: Int): Side = (White :: Black :: Nil)(i)
  }

  object Locations {
    implicit def fileToInt(file: File): Int = Location.Files.indexOf(file)

    implicit def rankToInt(rank: Rank): Int = Location.Ranks.indexOf(rank)

    implicit def intToFile(i: Int): File = Location.Files(i)

    implicit def intToRank(i: Int): Rank = Location.Ranks(i)

    implicit def locationToInt(location: Location): Int = location.rank * Board.Size + location.file
  }
}
