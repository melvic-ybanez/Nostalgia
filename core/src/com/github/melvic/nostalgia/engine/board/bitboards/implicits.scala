package com.github.melvic.nostalgia.engine.board.bitboards

import com.github.melvic.nostalgia.engine.base.Square
import com.github.melvic.nostalgia.engine.board.bitboards.Side.SideImplicits
import com.github.melvic.nostalgia.engine.board.bitboards.Square.SquareImplicits

trait implicits extends SideImplicits with SquareImplicits

object implicits extends implicits
