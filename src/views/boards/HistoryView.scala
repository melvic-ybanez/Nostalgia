package views.boards

import javafx.scene.control.ListView

import engine.board.{Notation, Piece}
import engine.movegen.Move.LocationMove

/**
  * Created by melvic on 1/23/19.
  */
class HistoryView extends ListView[String] {
  setFocusTraversable(false)
  setStyle("-fx-font-size: 14")

  def addMove(piece: Piece, move: LocationMove): Unit = {
    val moveNotation = Notation(piece, move).toString
    val moveNumber = "%2d".format(getItems.size + 1)
    val moveString = s"$moveNumber. $moveNotation"
    getItems.add(moveString)
  }
}
