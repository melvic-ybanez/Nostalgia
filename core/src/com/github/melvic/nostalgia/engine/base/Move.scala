package com.github.melvic.nostalgia.engine.base

final case class Move[L, M](from: L, to: L, moveType: M)