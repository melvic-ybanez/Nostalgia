package com.github.melvic.nostalgia.math

import com.github.melvic.nostalgia.math.NCell.{Col, Row}

final case class NCell(row: Row, col: Col)

object NCell {
  /**
    * Type-safe wrapper for an value representing row number
    */
  final case class Row(value: Int)

  /**
    * Type-safe wrapper for an value representing column number
    */
  final case class Col(value: Int)

  implicit class NCellOps(cell: NCell) {
    def toPlane[C](implicit plane: NPlane[C]): C =
      NPlane[C].initBounded(
        cell.col.value * plane.cellSize,
        cell.row.value * plane.cellSize
      )
  }
}
