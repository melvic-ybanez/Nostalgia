package engine.eval

import engine.board.bitboards.Bitboard
import engine.board.bitboards.Bitboard._
import engine.board._

/**
  * Created by melvic on 2/4/19.
  */
case class BitboardEvaluator(bitboard: Bitboard) {
  def evaluate(sideToMove: Side) = {
    def eval(side: Side) = {
      val pieceScores = (Pawn, 1) :: (Knight, 3) ::
        (Bishop, 3) :: (Rook, 5) :: (Queen, 9) ::
        (King, 200) :: Nil
      pieceScores.foldLeft(0) { case (total, (pieceType, score)) =>
        total + evalPiece(Piece(pieceType, side), score)
      }
    }

    eval(sideToMove) - eval(sideToMove.opposite)
  }

  def evalPiece(piece: Piece, score: Int) =
    serializeToStream(bitboard.pieceBitset(piece)).size * score
}
