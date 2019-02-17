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

  def pieceScoreMap: Piece => Double = { case Piece(pieceType, side) =>
    val pieceTypeMap: Map[PieceType, Double] = Map(Pawn -> 1,
      Knight -> 3, Bishop -> 3, Rook -> 5, Queen -> 9,
      King -> {
        /**
          * Make sure the side to move's king has a greater value to
          * prevent the computer from exchanging its king with yours.
          */
        if (side == sideToMove) 400 else 200
      })

    pieceTypeMap(pieceType)
  }

  def evaluate: Double = {
    def eval: SideScore = { side =>
      allPiecesScore(side) + materialScore(side)
    }

    eval(sideToMove) - eval(sideToMove.opposite)
  }

  def allPiecesScore: SideScore = side => Piece.types.foldLeft(0.0) {
    (total, pieceType) => total + pieceScore(Piece(pieceType, side))
  }

  def pieceScore(piece: Piece) =
    count(bitboard.pieceBitset(piece)) * pieceScoreMap(piece)

  def materialScore: SideScore = { side =>
    lazy val pawnCount = count(bitboard.pieceTypeBitsets(Pawn))
    lazy val pawn = Piece(Pawn, side)

    // A bishop pair is worth half the value of a pawn
    val  bishopsScore = {
      val bishopCount = count(bitboard.pieceBitset(Piece(Bishop, side)))
      if (bishopCount == 2) pieceScoreMap(pawn) / 2 else 0
    }

    // Increase the values of rooks as pawns disappear.
    val rooksScore = {
      val rookCount = count(bitboard.pieceBitset(Piece(Rook, side)))
      (8 - pawnCount) * 0.2 * rookCount
    }

    val pawnsScore = {
      // The absence of a single pawn should be penalized
      val pawnPresenceScore = if (pawnCount == 0) -0.2 else 0

      // Increased the values of central pawns
      val centralPawnMask = 0x0000001818000000L
      val centralPawnScore = positionScore(pawn, centralPawnMask, 0.5)

      // Decreased the values of rook pawns
      val rookPawnMask = 0x0081818181818100L
      val rookPawnPosition = positionScore(pawn, rookPawnMask, -0.2)

      // Penalize pawns that land on bad squares
      val badPawnPositionMask = 0x000000e7e7241800L :: 0x001824e7e7000000L :: Nil
      val pawnBitset = bitboard.pieceBitset(pawn)
      val badPawnPositionScore = count(badPawnPositionMask(side) & pawnBitset) * -0.2 * count(pawnBitset)

      pawnPresenceScore + centralPawnScore + rookPawnPosition + badPawnPositionScore
    }

    // Decrease the values of knights as pawns disappear
    val knightsScore = {
      val knightCount = count(bitboard.pieceBitset(Piece(Knight, side)))
      (8 - pawnCount) * -0.2 * knightCount
    }

    bishopsScore + rooksScore + pawnsScore + knightsScore
  }

  def positionScore(piece: Piece, mask: U64, score: Double): Double = {
    val pieceBitset = bitboard.pieceBitset(piece) & mask
    val pieceCount = count(pieceBitset)
    pieceCount * score
  }
}
