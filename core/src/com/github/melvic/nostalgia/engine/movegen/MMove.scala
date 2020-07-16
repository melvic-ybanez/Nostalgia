package com.github.melvic.nostalgia.engine.movegen

import com.github.melvic.nostalgia.engine.base
import com.github.melvic.nostalgia.engine.board.Piece
import com.github.melvic.nostalgia.engine.movegen.Location._

/**
  * Created by melvic on 8/5/18.
  */
object MMove {
  /**
    * Makes the coordinates compatible with the board view's
    * because the direction of the ranks are reversed
    */
  def locateMove(move: LocationMove) = {
    val source = Location.locateForView(move.source.rank, move.source.file)
    val dest = Location.locateForView(move.destination.rank, move.destination.file)
    (source, dest)
  }
}



