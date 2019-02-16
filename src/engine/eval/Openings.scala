package engine.eval

import engine.board.bitboards.Bitboard._
import engine.board.bitboards.Bitboard.U64
import engine.board.{Bishop, Pawn, PieceType}
import engine.movegen.{E, H, _4, _5}

/**
  * Created by melvic on 2/17/19.
  */
object Openings {
  lazy val FriedLiverAttack = Map(
    Pawn -> (singleBitset(E(_4)) | singleBitset(E(_5)))

  )
}
