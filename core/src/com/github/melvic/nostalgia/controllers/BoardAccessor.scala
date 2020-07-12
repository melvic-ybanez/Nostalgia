package com.github.melvic.nostalgia.controllers

import com.github.melvic.nostalgia.engine.board.{Board, Piece}
import com.github.melvic.nostalgia.engine.movegen.Location._
import com.github.melvic.nostalgia.engine.movegen.MMove.LocationMove
import com.github.melvic.nostalgia.engine.movegen.{Location, MMove}

/**
  * Created by melvic on 9/14/18.
  */
trait BoardAccessor {
  def apply(location: Location): Option[Piece] = board(accessorLocation(location))
  def apply(row: Int, col: Int): Option[Piece] = apply(locateForView(row, col))
  def board: Board

  def accessorLocation: Location => Location = identity
  def accessorMove: LocationMove => LocationMove = MMove.transform(accessorLocation)
  def move(source: Location, destination: Location): LocationMove =
    accessorMove(MMove[Location](source, destination))

  def moveBoard(move: MMove[Location]): Option[(BoardAccessor, Piece, Boolean)] = {
    val netMove = accessorMove(move)
    board(netMove.source).flatMap { piece =>
      val newBoard = board.updateByMove(netMove, piece)
      if (newBoard.isChecked(piece.side)) None
      else Some(updatedBoard(newBoard), piece, newBoard.isCheckmate(piece.side))
    }
  }

  def updatedBoard(f: => Board): BoardAccessor = {
    val accessor = this match {
      case SimpleBoardAccessor(_) => SimpleBoardAccessor
      case RotatedBoardAccessor(_) => RotatedBoardAccessor
    }
    accessor(f)
  }

  lazy val isRotated = this match {
    case SimpleBoardAccessor(_) => false
    case RotatedBoardAccessor(_) => true
  }
}

case class SimpleBoardAccessor(board: Board) extends BoardAccessor

case class RotatedBoardAccessor(board: Board) extends BoardAccessor {
  override def accessorLocation = location =>
    Location(Board.Size - 1 - location.file, Board.Size -1 - location.rank)
}

