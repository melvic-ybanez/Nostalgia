package engine.board.bitboards

import Bitboard.U64

/**
  * Created by melvic on 8/6/18.
  */
object Transformers {
  type Transformation = U64 => U64

  def horizontalMirror: Transformation = bitboard => {
    val singleColMask = 0x5555555555555555L
    val doubleColMask = 0x3333333333333333L
    val quadColMask = 0x0f0f0f0f0f0f0f0fL

    // shift even files to the left, odd files to the right, and combine the results
    var mirroredBoard = ((bitboard >> 1) & singleColMask) | ((bitboard & singleColMask) << 1)

    // do the same thing with the previous result, but shift twice as much
    mirroredBoard = ((mirroredBoard >> 2) & doubleColMask) | ((mirroredBoard & doubleColMask) << 2)

    // do the same thing with the previous result, but shift twice as much (four times the original)
    ((mirroredBoard >> 4) & quadColMask) | ((mirroredBoard & quadColMask) << 4)
  }

  def verticalFlip: Transformation = bitboard => {
    val singleRowMask = 0x00ff00ff00ff00ffL
    val doubleRowMask = 0x0000ffff0000ffffL

    // shift even ranks downward, odd ranks upward, and combine the result
    var flippedBoard = ((bitboard >> 8) & singleRowMask) | ((bitboard & singleRowMask) << 8)

    // do the same thing with the previous result, but shift twice as much
    flippedBoard = ((flippedBoard >> 16) & doubleRowMask) | ((flippedBoard & doubleRowMask) << 16)

    // do the same thing with the previous result, but shift twice as much (four times the original)
    (flippedBoard >> 32) | (flippedBoard << 32)
  }

  def rotate180: Transformation = horizontalMirror andThen verticalFlip
}
