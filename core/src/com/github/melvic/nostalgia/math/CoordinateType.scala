package com.github.melvic.nostalgia.math

trait CoordinateType[C] {
  type V
}

object CoordinateType {
  type Aux[A, B] = CoordinateType[A] {
    type V = B
  }

  implicit val boardCoordinateType: CoordinateType[NBoard] =
    new CoordinateType[NBoard] {
      override type V = Int
    }

  implicit val canvasCoordinateType: CoordinateType[NCanvas[_]] =
    new CoordinateType[NCanvas[_]] {
      override type V = Double
    }
}
