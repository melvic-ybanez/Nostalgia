package engine.eval

import engine.board.Piece._
import engine.board.bitboards.Bitboard
import engine.board.bitboards.Bitboard._
import engine.board._
import engine.movegen._

/**
  * Created by melvic on 2/4/19.
  */
case class BitboardEvaluator(bitboard: Bitboard, sideToMove: Side) {
  type SideScore = Side => Double

  def pieceScoreMap(side: Side): Map[PieceType, Integer] = Map(Pawn -> 1,
    Knight -> 3, Bishop -> 3, Rook -> 5, Queen -> 9,
    King -> {
      /**
        * Make sure the side to move's king has a greater value to
        * prevent the computer from exchanging its king with yours.
        */
      if (side == sideToMove) 400 else 200
    })

  def evaluate: Double = {
    def eval: SideScore = { side =>
      allPiecesScore(side) + materialScore(side)
    }

    eval(sideToMove) - eval(sideToMove.opposite)
  }

  def allPiecesScore: SideScore = side => pieceScoreMap(side).foldLeft(0) {
    case (total, (pieceType, _)) => total + pieceScore(Piece(pieceType, side))
  }

  def pieceScore(piece: Piece) =
    isolate(bitboard.pieceBitset(piece)).size * pieceScoreMap(piece.side)(piece.pieceType)

  def materialScore: SideScore = { side =>
    lazy val pawnCount = count(bitboard.pieceTypeBitsets(Pawn))

    // A bishop pair is worth half the value of a pawn
    val  bishopPair = {
      val bishopCount = count(bitboard.pieceBitset(Piece(Bishop, side)))
      if (bishopCount == 2) pieceScoreMap(side)(Pawn) / 2 else 0
    }

    // Increase the value of a rook if there are fewer pawns on the board.
    val rookBonus = {
      val rookCount = count(bitboard.pieceBitset(Piece(Rook, side)))
      (8 - pawnCount) * 0.2 * rookCount
    }

    val pawnBonus = {
      // The absence of a single pawn should be penalized
      val pawnPresenceScore = if (pawnCount == 0) -0.2 else 0

      val pawn = Piece(Pawn, side)

      // Increased the value of central pawns
      val centralPawnMask = 0x0000001818000000L
      val centralPawnScore = positionScore(pawn, centralPawnMask, 0.2)

      // Decreased the value of rook pawns
      val rookPawnMask = 0x0081818181818100L
      val rookPawnPosition = positionScore(pawn, rookPawnMask, -0.1)

      pawnPresenceScore + centralPawnScore + rookPawnPosition
    }

    bishopPair + rookBonus + pawnBonus
  }

  def positionScore(piece: Piece, mask: U64, score: Double): Double = {
    val pieceBitset = bitboard.pieceBitset(piece) & mask
    val pieceCount = count(pieceBitset)
    pieceCount * score
  }
}
