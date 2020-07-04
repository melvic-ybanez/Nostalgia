package com.github.melvic.nostalgia.math

trait Num[A] {
  type V

  def of(value: V): V = value
}

object Num {
  def apply[A](implicit num: Num[A]): Num[A] = num

  implicit val intNum: Num[Int] = new Num[Int] {
    override type V = Int
  }

  implicit val doubleNum: Num[Double] = new Num[Double] {
    override type V = Double
  }
}
