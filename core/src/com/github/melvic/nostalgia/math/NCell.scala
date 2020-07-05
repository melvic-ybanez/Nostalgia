package com.github.melvic.nostalgia.math

final case class NCell(col: Int, row: Int)

object NCell {
  implicit class NCellOps(cell: NCell) {
    def toCoordinate[C](implicit plane: NCoordinate[C]): C =
      NCoordinate[C].initBounded(
        cell.col * plane.size,
        cell.row * plane.size
      )
  }
}
