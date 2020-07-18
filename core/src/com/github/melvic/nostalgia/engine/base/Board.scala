package com.github.melvic.nostalgia.engine.base

/**
 * The base typeclass representing a chessboard
 * @tparam B The board type
 * @tparam T The type for piece type (e.g. pawns, kings, etc.)
 * @tparam S The type for sides.
 * @tparam L The type to use for the locations (e.g integers for bitboards)
 */
trait Board[B, T, S, L] {
  type BPiece = Piece[T, S]
  type BMove = Move[T, S, L]

  def at(board: B, location: L): Option[BPiece]

  def apply(board: B, location: L): Option[BPiece] = at(board, location)

  def updateByMove(board: B, move: BMove, piece: BPiece): B

  def lastMove(board: B): Option[BMove]

  def generateMoves(board: B, sideToMove: S): List[(BMove, BPiece)]

  def updateByNextMove(board: B, sideToMove: S, depth: Int): B

  def isChecked(board: B, side: S): Boolean
  def isCheckmate(board: B, winningSide: S): Boolean

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
  def canCastle(board: B, kingMove: BMove): Boolean

  def pieceLocations(board: B, piece: BPiece): List[L]

  val Size = 8
}

object Board {
  def apply[B, T, S, L](implicit B: Board[B, T, S, L]): Board[B, T, S, L] = B
}

