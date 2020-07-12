package com.github.melvic.nostalgia.engine.base

import scala.language.higherKinds

final case class Move[T, S, L](from: L, to: L, moveType: MoveType[Piece[T, S]])