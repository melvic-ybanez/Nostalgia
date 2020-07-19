package com.github.melvic.nostalgia.engine.board.bitboards

import com.github.melvic.nostalgia.engine.board.bitboards.Side.SideImplicits

trait implicits extends SideImplicits with SquareImplicits

object implicits extends implicits
