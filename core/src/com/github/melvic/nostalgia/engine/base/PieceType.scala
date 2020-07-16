package com.github.melvic.nostalgia.engine.base

trait PieceType[T] {
  def pawn: T
  def knight: T
  def bishop: T
  def rook: T
  def queen: T
  def king: T
}