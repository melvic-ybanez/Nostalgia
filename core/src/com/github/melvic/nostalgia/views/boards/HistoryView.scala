package com.github.melvic.nostalgia.views.boards

import com.github.melvic.nostalgia.engine.board.{Board, Notation, Piece, White}
import com.github.melvic.nostalgia.engine.movegen.Move.LocationMove
import javafx.geometry.{Insets, Orientation}
import javafx.scene.control.{Label, ListCell, ListView, Separator}
import javafx.scene.layout.VBox

/**
  * Created by melvic on 1/23/19.
  */
class HistoryView extends VBox {
  val themeColor = "white"

  class HistoryListView extends ListView[String] {
    setFocusTraversable(false)
    setStyle(s"-fx-border-color: $themeColor;")
    setPadding(new Insets(0, 0, 0, 0))

    setCellFactory(_ => new ListCell[String] {
      override def updateItem(item: String, empty: Boolean): Unit = {
        super.updateItem(item, empty)
        setStyle("-fx-background-color: " + themeColor)
        setText(null)
        setGraphic(null)

        if (!empty) setText(item)
      }
    })
  }

  val listView = new HistoryListView

  val titlePane = new VBox
  val titleLabel = new Label("History")
  titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold")
  titlePane.getChildren.add(titleLabel)
  titlePane.setPadding(new Insets(10))

  getChildren.addAll(titlePane, new Separator(Orientation.HORIZONTAL), listView)

  def addMove(move: LocationMove, board: Board, piece: Piece): Unit = {
    val moveNotation = Notation.ofMove(move, piece, board)

    if (piece.side == White) {
      val moveNumber = "%2d".format(listView.getItems.size + 1)
      val moveString = s"$moveNumber. $moveNotation"
      listView.getItems.add(moveString)
    } else {
      val lastIndex = listView.getItems.size - 1
      val lastItem = listView.getItems.get(lastIndex)
      listView.getItems.set(lastIndex, s"$lastItem $moveNotation")
    }
  }
}
