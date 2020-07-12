package com.github.melvic.nostalgia.engine.base

import scala.language.higherKinds

final case class Move[P, M[_]](from: P, to: P, moveType: M[P])