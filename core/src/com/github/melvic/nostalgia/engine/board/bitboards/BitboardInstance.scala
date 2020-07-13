package com.github.melvic.nostalgia.engine.board.bitboards

import com.github.melvic.nostalgia.engine.base.MoveType.{Castling, DoublePawnPush, EnPassant, PawnPromotion}
import com.github.melvic.nostalgia.engine.base.Square
import com.github.melvic.nostalgia.engine.board.Board
import com.github.melvic.nostalgia.engine.movegen.Location.intToLocation
import com.github.melvic.nostalgia.engine.movegen.bitboards.BitboardMoveGenerator


case class BitboardInstance(
    bitsets: Vector[U64],
    castlingBitsets: Vector[U64],
    lastBitboardMove: Option[Move],
    enPassantBitset: U64 = 0L) {

  import Bitboard._

  lazy val (sideBitsets, pieceTypeBitsets) = bitsets.splitAt(PieceTypeOffset)

  def updatePiece(piece: Piece)(f: U64 => U64): BitboardInstance = piece match {
    case Piece(pieceType, side) =>
      val pieceTypeIndex = pieceType + PieceTypeOffset
      val updatedPieceType = bitsets.updated(pieceTypeIndex, f(bitsets(pieceTypeIndex)))
      val updateBitSets = updatedPieceType.updated(side, f(updatedPieceType(side)))
      BitboardInstance(updateBitSets, castlingBitsets, lastBitboardMove)
  }

  def updateByMove(move: Move, piece: Piece) =
    updateByBitboardMove(Move(move.from, move.to, move.moveType), piece)

  def updateByBitboardMove(move: Move, piece: Piece): BitboardInstance = {
    val sourceBitset = singleBitset(move.from)
    val destBitset = singleBitset(move.to)
    val moveBitset = sourceBitset ^ destBitset

    // handle captures
    val oppositeSide = piece.side.opposite
    val oppositeSideBitset = sideBitsets(oppositeSide)
    val capturedIndex = pieceTypeBitsets.indexWhere { bitset =>
      val oppositePieceTypeBitset = bitset & oppositeSideBitset
      isNonEmptySet(oppositePieceTypeBitset & destBitset)
    }

    val captureBoard =
      if (capturedIndex == -1) this
      else updatePiece(Piece(capturedIndex, oppositeSide))( _ ^ destBitset)

    val partialBoard = captureBoard.updatePiece(piece)(_ ^ moveBitset).updateLastMove(move)
      .withCastlingBitsets(castlingBitsets.map { bitset =>
        if (isNonEmptySet(bitset & sourceBitset)) bitset ^ sourceBitset
        else bitset
      })

    // handle special cases
    move.moveType match {
      case EnPassant =>
        partialBoard.updatePiece(Piece(Pawn, oppositeSide)) { _ ^ {
          if (piece.side == White) destBitset >> Board.Size
          else destBitset << Board.Size
        }}
      case PawnPromotion(promotionPiece) =>
        // remove the pawn and replace it with the specified officer
        partialBoard.updatePiece(piece)( _ ^ destBitset)
          .updatePiece(promotionPiece)(_ ^ destBitset)
      case DoublePawnPush => partialBoard.withEnPassantBitset(destBitset)
      case Castling => partialBoard.castle(move, piece.side)
      case _ => partialBoard
    }
  }

  def castle(kingMove: Move, side: Side) = {
    val index = castlingRookIndex(kingMove, side)

    val updatedBoard = updatePiece(Piece(Rook, side)) { rookBitset =>
      val castlingRookBitset = castlingBitsets(index) & sideBitsets(side)
      val movedCastlingRookBitset =
        if (index == KingSideCastlingIndex) castlingRookBitset >>> 2
        else castlingRookBitset << 3

      rookBitset ^ castlingRookBitset | movedCastlingRookBitset
    }

    val updatedCastlingBitsets = castlingBitsets
      // remove the king
      .updated(0, castlingBitsets(0) & pieceTypeBitsets(King))

      // remove the rook
      .updated(index, castlingBitsets(index) & updatedBoard.pieceTypeBitsets(Rook))

    updatedBoard.withCastlingBitsets(updatedCastlingBitsets)
  }

  def updateLastMove(move: Move) = BitboardInstance(bitsets, castlingBitsets, Some(move))

  def withCastlingBitsets(castlingBitsets: Vector[U64]) =
    BitboardInstance(bitsets, castlingBitsets, lastBitboardMove)

  def withEnPassantBitset(enPassantBitset: U64) =
    BitboardInstance(bitsets, castlingBitsets, lastBitboardMove, enPassantBitset)

  def at(position: Int): Option[Piece] = at(Bitboard.singleBitset(position))

  def at(bitset: U64): Option[Piece] = {
    val sideIndex = sideBitsets.indexWhere(intersectedWith(bitset))
    if (sideIndex == -1) None
    else {
      val pieceIndex = pieceTypeBitsets.indexWhere(intersectedWith(bitset))
      if (pieceIndex == -1) None
      else Some(Piece(pieceIndex, sideIndex))
    }
  }

  def lastMove = lastBitboardMove.map { move =>
    Move(move.from, move.to, move.moveType)
  }

  def pieceBitset: Piece => U64 = { case Piece(pieceType, side) =>
    sideBitsets(side) & pieceTypeBitsets(pieceType)
  }

  def apply: Int => Option[Piece] = at

  def whitePieces = bitsets(White)
  def blackPieces = bitsets(Black)

  def occupied = whitePieces | blackPieces
  def emptySquares = ~occupied
  def opponents(side: Side) = bitsets(side.opposite)

  def isChecked(side: Side) = {
    val kingPosition = bitScan(pieceBitset(side.of(King)))
    val moveGenerators = types.map(pieceType => (BitboardMoveGenerator.moveGenerator, pieceType))

    moveGenerators.exists { case (generatorType, attacker) =>
      val generator = generatorType(attacker)
      generator.attackBitsets(this, kingPosition, side) exists { destination =>
        at(destination).exists { case Piece(destType, destSide) =>
          attacker == destType && destSide == side.opposite
        }
      }
    }
  }

  def isCheckmate(winningSide:Side) = {
    val loosingSide = winningSide.opposite
    val piecesToMove = Pawn :: Knight :: Bishop :: Rook :: Queen :: King :: Nil

    isChecked(loosingSide) && !piecesToMove.exists { pieceType =>
      val piece = Piece(pieceType, loosingSide)
      val pieceIndexes = toSquareIndexes(pieceBitset(piece))
      pieceIndexes.exists { squareIndex =>
        val moveGenerator = BitboardMoveGenerator.moveGenerator(pieceType)
        moveGenerator.validMoves(this, squareIndex, loosingSide)
          .exists(!updateByBitboardMove(_, piece).isChecked(loosingSide))
      }
    }
  }

  def canCastle(kingMove: Move) = at(kingMove.from) exists {
    case piece@Piece(King, side) =>
      val kingBitset = pieceBitset(piece)
      val sideBitset = sideBitsets(side)

      val kingHasBeenMoved = isEmptySet(castlingBitsets(0) & kingBitset)
      val rookHasBeenMoved = {
        val kingBitboardMove = Move.normal(kingMove.from, kingMove.to)
        val index = castlingRookIndex(kingBitboardMove, side)
        isEmptySet(castlingBitsets(index) & sideBitset)
      }
      !kingHasBeenMoved && !rookHasBeenMoved
    case _ => false
  }

  def castlingRookIndex(kingMove: Move, side: Side) = {
    val delta = kingMove.to.file - kingMove.from.file
    if (delta < 0) QueenSideCastlingIndex else KingSideCastlingIndex
  }

  def generateMoves(sideToMove: Side) = types.flatMap { pieceType =>
    val moveGenerator = BitboardMoveGenerator.moveGenerator(pieceType)
    val piece = Piece(pieceType, sideToMove)
    val piecePositions = toSquareIndexes(pieceBitset(piece))
    piecePositions.flatMap { source =>
      val moves = moveGenerator.validMoves(this, source, sideToMove)
      moves.map(move => (move, piece))
    }
  }

  def updateByNextMove(sideToMove: Side, depth: Int) =
    AlphaBetaMax.search(this, sideToMove, -Integer.MAX_VALUE, Integer.MAX_VALUE, depth)._2

  override def pieceLocations(piece: Piece) =
    toSquareIndexes(pieceBitset(piece)).map(intToLocation)

  override def toString = {
    def sideString: Side => String = List("W", "B")(_)
    def pieceTypeString: PieceType => String = "PNBRQK"(_).toString
    val space = " " * 2

    val squareStrings = (0 until 64).foldLeft[List[String]](Nil) { (acc, i) =>
      val squareString = this.apply(i).map { case Piece(pieceType, side) =>
        sideString(side) + pieceTypeString(pieceType)
      } getOrElse ("_" * space.length)

      if (i % Board.Size == 0) squareString :: acc
      else acc.head + space + squareString :: acc.tail
    }

    squareStrings.mkString("\n")
  }
}

object BitboardInstance {
  implicit val bitboardBoard: Bitboard = new Bitboard {
    override implicit def square: Square[Int] = Square[Int]

    override def at(board: BitboardInstance, location: Int) = board.at(location)

    override def updateByMove(board: BitboardInstance, move: BMove, piece: BPiece) =
      board.updateByMove(move, piece)

    override def lastMove(board: BitboardInstance) = board.lastMove

    override def generateMoves(board: BitboardInstance, sideToMove: Int) =
      board.generateMoves(sideToMove)

    override def updateByNextMove(board: BitboardInstance, sideToMove: Int, depth: Int) =
      board.updateByNextMove(sideToMove, depth)

    override def isChecked(board: BitboardInstance, side: Int) = board.isChecked(side)

    override def isCheckmate(board: BitboardInstance, winningSide: Int) = board.isCheckmate(winningSide)
  }
}

