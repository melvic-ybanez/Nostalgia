package com.github.melvic.nostalgia.engine.base

/**
 * The base type representing a chessboard
 * @tparam T The type for piece type (e.g. pawns, kings, etc.)
 * @tparam S The type for sides.
 * @tparam L The type to use for the locations (e.g integers for bitboards)
 */
trait Board[T, S, L] {
  type BPiece = Piece[T, S]
  type BMove = Move[T, S, L]

  def at(location: L): Option[BPiece]

  def apply(location: L): Option[BPiece] = at(location)

  def updateByMove(move: BMove, piece: BPiece): Board[T, S, L]

  def lastMove: Option[BMove]

  def generateMoves(sideToMove: S): List[(BMove, BPiece)]

  def updateByNextMove(sideToMove: S, depth: Int): Board[T, S, L]

  def isChecked(side: S): Boolean
  def isCheckmate(winningSide: S): Boolean

  /**
    * Checks if the king, given its move, can still castle. That is, if the king
    * and the rook has not moved yet.
    * Note that this method does not check if a castle is possible with the current
    * board position. For instance, if the path to the castling destination is blocked or
    * is being threaten by an opponent piece, this method might still return true if the
    * conditions above are met.
    * @param kingMove Location of the king
    * @return Whether the king can still castle.
    */
  def canCastle(kingMove: BMove): Boolean

  def pieceLocations(piece: BPiece): List[L]
}

object Board {
  val Size = 8
}

