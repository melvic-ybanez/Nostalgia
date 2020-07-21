package com.github.melvic.nostalgia.engine.api.board

import cats.{Bifunctor, Functor}
import com.github.melvic.nostalgia.engine.api.movegen.Location
import com.github.melvic.nostalgia.engine.api.piece.Side.{Black, White}
import com.github.melvic.nostalgia.engine.api.piece.{PieceType, Side}
import com.github.melvic.nostalgia.engine.base.{Move, Board => BaseBoard, Piece => BasePiece}
import com.github.melvic.nostalgia.engine.board.bitboards.{
  Bitboard,
  Piece => BBPiece,
  PieceType => BBPieceType,
  Side => BBSide,
}
import com.github.melvic.nostalgia.math.Trifunctor

final case class Board(bitboard: Bitboard) extends BaseBoard[PieceType, Side, Location] {
  override def at(location: Location) =
    bitboard.at(location.toBitPosition).map(
      Bifunctor[BasePiece].bimap(_)(PieceType.all, Side.all))

  override def updateByMove(move: BMove, piece: BPiece) = {
    def pieceTypeToInt(pieceType: PieceType) =
      BBPieceType.all(PieceType.all.indexOf(pieceType))

    def sideToInt(side: Side) = BBSide.all(Side.all.indexOf(side))

    val updatedBitBoard = bitboard.updateByMove(
      Trifunctor[Move].trimap(move)(pieceTypeToInt, sideToInt, _.toBitPosition),
      Bifunctor[BasePiece].bimap(piece)(pieceTypeToInt, sideToInt)
    )
    Board(updatedBitBoard)
  }

  override def lastMove = ???

  override def generateMoves(sideToMove: Side) = ???

  override def updateByNextMove(sideToMove: Side, depth: Int) = ???

  override def isChecked(side: Side) = ???

  override def isCheckmate(winningSide: Side) = ???

  override def canCastle(kingMove: BMove) = ???

  override def pieceLocations(piece: BPiece) = ???
}
