package com.github.melvic.nostalgia.engine.board.bitboards

import com.github.melvic.nostalgia.engine.board.bitboards.Bitboard.U64

/**
  * Created by melvic on 8/6/18.
  */
object Transformers {
  type Transformation = U64 => U64

  /**
    * Shift even files to the left, odd files to the right, and combine the results.
    * Do this recursively. In each iteration, double the column mask and steps.
    */
  def horizontalMirror: Transformation = flip(List(
    0x5555555555555555L,
    0x3333333333333333L,
    0x0f0f0f0f0f0f0f0fL
  ), 1)

  /**
    * Shift even ranks downward, odd ranks upward, and combine the result.
    * Do this recursively. In each iteration, double the row mask and steps.
    */
  def verticalFlip: Transformation = flip(List(
    0x00ff00ff00ff00ffL,
    0x0000ffff0000ffffL
  ), 8) andThen { flipped => (flipped >> 32) | (flipped << 32) }


  def flip(masks: List[U64], initialStep: Int): Transformation = bitboard =>
    masks.foldLeft(bitboard, initialStep) { case ((bitset, step), mask) =>
      (((bitset >> step) & mask) | ((bitset & mask) << step), step * 2)
    }._1

  def rotate180: Transformation = horizontalMirror andThen verticalFlip
}
