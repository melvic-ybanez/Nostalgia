package tests.tests.board

import engine.board.bitboards.Bitboard
import org.scalatest.{FlatSpec, FunSuite}

/**
  * Created by melvic on 8/8/18.
  */
class InitializationSpec extends FlatSpec {
  val board = Bitboard.initialize

  "An initalized board" should "have white pawns on Rank 2" in {
    val whitePawns = board.pawns & board.whitePieces
  }
}
