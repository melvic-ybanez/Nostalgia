package engine.movegen.bitboards

import engine.board.bitboards.Bitboard.U64
import engine.board.bitboards.{Bitboard, Transformers}
import engine.board.{Board, Piece}
import engine.movegen.Attack

import scala.annotation.tailrec

/**
  * Created by melvic on 9/23/18.
  */
trait SlidingMoveGenerator extends BitboardMoveGenerator {
  /**
    * A mapping of a slider position and a bitset to a bitset
    */
  type Slide = (Int, U64) => U64

  object Masks {
    lazy val Files = (0 until Board.Size).map { i =>
      // position the one-bit (representing the file) in the rank
      val rank = Math.pow(2, i).toLong

      // replicate the rank
      (1 until Board.Size).foldLeft(rank) { (bitset, rankIndex) =>
        bitset | (bitset << Board.Size)
      }
    }

    /**
      * A sequence of bitsets, each having at least one rank
      * filled with 1s at a given file.
      */
    lazy val Ranks = (0 until Board.Size).map(file => 0xffL << Board.Size * file)

    /*lazy val Diagonals = diagonals(bitset => oneStep.northEast(bitset) | oneStep.southWest(bitset))
    lazy val Diagonals = Array(
      0x8040201008040201L, 0x4020100804020100L,
      0x2010080402010000L, 0x1008040201000000L,
      0x0804020100000000L, 0x0402010000000000L,
      0x0201000000000000L, 0x0100000000000000L,
      0x0000000000000000L, 0x0000000000000080L,
      0x0000000000008040L, 0x0000000000804020L,
      0x0000000000804010L, 0x0000000080401008L,
      0x0000008040100804L, 0x0000804010080402L
    )*/
    def diagonals = (0 until Board.Size * 2).map { i =>
      val oneStep = new PostShiftOneStep {}
      val init: Long = 1 << Board.Size * i
      (i + 1 until Board.Size).foldLeft(init) { (bitset, _) =>
        bitset | oneStep.northEast(bitset)
      }
    }

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
    lazy val Diagonals = {
      val upperDiagonals = diagonals
      val lowerDiagonals = (1 until Board.Size).map(i => Transformers.rotate180(upperDiagonals(i)))
      upperDiagonals ++ IndexedSeq(0L) ++ lowerDiagonals
    }

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
    //lazy val AntiDiagonals = diagonals(bitset => oneStep.northWest(bitset) | oneStep.southEast(bitset))
    lazy val AntiDiagonals = Array(

    )
  }

  /**
    * Multiply the slider by 2 to move it one step closer to the blocker.
    * Then subtract the product from the board of occupied squares to
    * flip the zeroes between the slider and the blocker to ones (due
    * to borrowing), thereby turning the blocker into zero. Then xor the
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
    */
  def negativeSlide: Slide = { (sliderPosition, occupied) =>
    val transform = Transformers.rotate180

    val slider = Bitboard.singleBitset(sliderPosition)
    val reversedSlider = transform(slider)
    val reversedOccupied = transform(occupied)

    occupied ^ transform(reversedOccupied - 2 * reversedSlider)
  }

  def ray(slide: Slide)(masker: Int => U64): Slide = { (sliderPosition, occupied) =>
    val mask = masker(sliderPosition)
    val targetSquares = occupied & mask
    slide(sliderPosition, targetSquares) & mask
  }

  lazy val positiveRay: (Int => U64) => Slide = ray(positiveSlide)
  lazy val negativeRay: (Int => U64) => Slide = ray(negativeSlide)

  def fileMask(sliderPosition: Int) = Masks.Files(sliderPosition % Board.Size)

  def rankMask(sliderPosition: Int) = Masks.Ranks(sliderPosition / Board.Size)

  def diagonalMask(sliderPosition: Int) = Masks.Diagonals(sliderPosition)

  def antiDiagonalMask(sliderPosition: Int) = Masks.AntiDiagonals(sliderPosition)

  def moves: Stream[Slide]

  override def destinationBitsets: StreamGen[WithMove[U64]] = { (board, source, side) =>
    moves flatMap { slide =>
      val moveBitset = slide(source, board.occupied)
      val blocker = board.occupied & moveBitset

      // remove the blocker from the set of valid destinations if it's not an enemy and is not empty
      val validMoveBitSet = board(Bitboard.oneBitIndex(blocker)) match {
        case Some(Piece(_, blockerSide)) if blockerSide == side =>
          moveBitset ^ blocker
        case _ => moveBitset
      }

      Bitboard.serializeToStream(validMoveBitSet).map((_, Attack))
    }
  }
}
