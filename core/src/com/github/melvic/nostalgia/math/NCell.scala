package com.github.melvic.nostalgia.math

final case class NCell(row: Int, col: Int)

object NCell {
  implicit class NCellOps(cell: NCell) {
    def toCanvas[C](implicit canvas: NCanvas[C]): C =
      NCanvas[C].initBounded(
        cell.col * canvas.squareSize,
        cell.row * canvas.squareSize
      )
  }
}
