package com.github.melvic.nostalgia.engine.api.piece

import com.github.melvic.nostalgia.engine.base.{Piece => BasePiece}

object Piece {
  type Piece = BasePiece[PieceType, Side]
}
