package com.github.melvic_ybanez.nostalgia.engine.movegen.bitboards

import com.github.melvic_ybanez.nostalgia.engine.board.bitboards.Bitboard.U64
import com.github.melvic_ybanez.nostalgia.engine.board.bitboards.Transformers.Transformation
import com.github.melvic_ybanez.nostalgia.engine.board.bitboards.{Bitboard, Transformers}
import com.github.melvic_ybanez.nostalgia.engine.board.{Board, Piece}
import com.github.melvic_ybanez.nostalgia.engine.movegen.Attack


/**
  * Created by melvic on 9/23/18.
  */
object SlidingMoveGenerator {
  object Masks {
    lazy val Files = (0 until Board.Size).map { i =>
      // position the one-bit (representing the file) in the rank
      val file = Math.pow(2, i).toLong

      // replicate the rank
      (1 until Board.Size).foldLeft(file) { (bitset, rankIndex) =>
        bitset | (bitset << Board.Size)
      }
    }

    /**
      * A sequence of bitsets, each having at least one rank
      * filled with 1s at a given file.
      */
    lazy val Ranks = (0 until Board.Size).map(file => 0xffL << Board.Size * file)

    def diagonals(start: Int => Int, step: OneStep.Step) = {
      def half(initStart: Int => Int, initStep: Int => Int) =
        (0 until Board.Size).map { i =>
          val init = (1: U64) << Board.Size * start(initStart(i)) + initStep(i)
          (i + 1 until Board.Size).foldLeft(init) { (bitset, _) =>
            bitset | step(bitset)
          }
        }

      val upperDiagonals = half(identity, _ => 0)
      val lowerDiagonals = half(_ => 0, identity)
      val index8 = IndexedSeq(0L)

      upperDiagonals ++ index8 ++ lowerDiagonals.tail.reverse
    }

    lazy val oneStep = new PostShiftOneStep {}

    /**
      * 07 06 05 04 03 02 01 00
      * 06 05 04 03 02 01 00 15
      * 05 04 03 02 01 00 15 14
      * 04 03 02 01 00 15 14 13
      * 03 02 01 00 15 14 13 12
      * 02 01 00 15 14 13 12 11
      * 01 00 15 14 13 12 11 10
      * 00 15 14 13 12 11 10 09
      */
    lazy val Diagonals: IndexedSeq[U64] = diagonals(identity, oneStep.northEast)

    /**
      * 00 15 14 13 12 11 10 09
      * 01 00 15 14 13 12 11 10
      * 02 01 00 15 14 13 12 11
      * 03 02 01 00 15 14 13 12
      * 04 03 02 01 00 15 14 13
      * 05 04 03 02 01 00 15 14
      * 06 05 04 03 02 01 00 15
      * 07 06 05 04 03 02 01 00
      */
    lazy val AntiDiagonals = diagonals(Board.Size - 1 - _, oneStep.southEast)
  }

  def stringifyMask(xs: IndexedSeq[U64]): String = {
    val tableSize = Board.Size

    def row(rowNumber: Int) = (0 until tableSize).map { i =>
      xs.indexWhere { x =>
        val step = rowNumber * tableSize + i
        val bitset = (1: U64) << step
        Bitboard.isNonEmptySet(x & bitset)
      }
    }

    val table = (0 until tableSize).map(row).reverse
    table.map(_.map("%02d".format(_)).mkString(" ")).mkString("\n")
  }
}

trait SlidingMoveGenerator extends BitboardMoveGenerator {

  import SlidingMoveGenerator._

  /**
    * A mapping of a slider position and a bitset to a bitset
    */
  type Slide = (Int, U64) => U64
  type Mask = Int => U64

  /**
    * Multiplies the slider by 2 to move it one step closer to the blocker.
    * It then subtracts the product from the board of occupied squares to
    * flip the zeroes between the slider and the blocker to ones (due
    * to borrowing), thereby turning the blocker into zero. Then xors the
    * result with the occupied squares to toggle the bits. The blocker
    * shall be set back to one, the slider to zero, the empty squares
    * between them to ones, and the rest to zeroes.
    */
  def positiveSlide: Slide = { (sliderPosition, occupied) =>
    val slider = Bitboard.singleBitset(sliderPosition)
    occupied ^ (occupied - 2 * slider)
  }

  /**
    * The "reversed" version of the algorithm for the positive ray.
    * The reverse function is needed for the reverse arithmetic.
    */
  def negativeSlide(reverse: Transformation): Slide = { (sliderPosition, occupied) =>
    val slider = Bitboard.singleBitset(sliderPosition)
    val reversedSlider = reverse(slider)
    val reversedOccupied = reverse(occupied)

    occupied ^ reverse(reversedOccupied - reversedSlider - reversedSlider)
  }

  def ray(slide: Slide)(masker: Mask): Slide = { (sliderPosition, occupied) =>
    val mask = masker(sliderPosition)
    val targetSquares = occupied & mask
    slide(sliderPosition, targetSquares) & mask
  }

  def positiveRay: Mask => Slide = ray(positiveSlide)

  def negativeRay(reverse: Transformation = java.lang.Long.reverseBytes): Mask => Slide =
    ray(negativeSlide(reverse))

  def fileMask(sliderPosition: Int) = Masks.Files(Bitboard.fileOf(sliderPosition))

  def rankMask(sliderPosition: Int) = Masks.Ranks(Bitboard.rankOf(sliderPosition))

  def diagonalMask: Mask = getDiagonalMask {
    (rankIndex, fileIndex) => Masks.Diagonals((rankIndex - fileIndex) & 15)
  }

  def antiDiagonalMask: Int => U64 = getDiagonalMask {
    (rankIndex, fileIndex) => Masks.AntiDiagonals((rankIndex + fileIndex) ^ 7)
  }

  /**
    * A function that retrieves the rank and file indexes of a given slider position,
    * and combine them into one index.
    * @param f A function that decides how to combine the rank and file indexes.
    * @param sliderPosition The position of the slider
    * @return The result of applying the function to the file and rank indexes.
    */
  def getDiagonalMask(f: (Int, Int) => U64)(sliderPosition: Int) = {
    val rankIndex = Bitboard.rankOf(sliderPosition)
    val fileIndex = Bitboard.fileOf(sliderPosition)
    f(rankIndex, fileIndex)
  }

  def moves: Stream[Slide]

  override def destinationBitsets: StreamGen[WithMove[U64]] = { (board, source, side) =>
    moves flatMap { slide =>
      val moveBitset = slide(source, board.occupied)
      val blocker = board.occupied & moveBitset

      // remove the blocker from the set if it's neither an enemy nor empty
      val validMoveBitSet = board(Bitboard.bitScan(blocker)) match {
        case Some(Piece(_, blockerSide)) if blockerSide == side =>
          moveBitset ^ blocker
        case _ => moveBitset
      }

      Bitboard.isolate(validMoveBitSet).map((_, Attack))
    }
  }
}
