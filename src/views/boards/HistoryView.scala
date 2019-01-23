package views.boards

import javafx.scene.control.ListView

import engine.board.{Board, Notation, Piece, White}
import engine.movegen.Move.LocationMove

/**
  * Created by melvic on 1/23/19.
  */
class HistoryView extends ListView[String] {
  setFocusTraversable(false)
  setStyle("-fx-font-size: 14")

  def addMove(move: LocationMove, board: Board, piece: Piece, checkmate: Boolean): Unit = {
    val moveNotation = Notation.of(move, board, checkmate)

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
