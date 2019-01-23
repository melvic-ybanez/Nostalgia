package views.boards

import javafx.scene.control.ListView

import engine.board._
import engine.movegen.Move.LocationMove

/**
  * Created by melvic on 1/23/19.
  */
class HistoryView extends ListView[String] {
  setFocusTraversable(false)
  setStyle("-fx-font-size: 15;")

  def addMove(move: LocationMove, board: Board, piece: Piece): Unit = {
    val moveNotation = Notation.of(move, piece, board)

    if (piece.side == White) {
      val moveNumber = "%2d".format(getItems.size + 1)
      val moveString = s"$moveNumber. $moveNotation"
      getItems.add(moveString)
    } else {
      val lastIndex = getItems.size - 1
      val lastItem = getItems.get(lastIndex)
      getItems.set(lastIndex, s"$lastItem $moveNotation")
    }
  }
}
