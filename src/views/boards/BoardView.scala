package views.boards

import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{BorderPane, GridPane}
import javafx.scene.paint.Color
import javafx.scene.text.{Font, Text, TextAlignment, TextFlow}

import controllers.BoardController
import engine.board.{Board, Piece}
import engine.movegen.Location
import engine.movegen.Location._
import events.{MoveEventHandler, PieceHoverEventHandler}
import main.Resources

/**
  * Created by melvic on 9/11/18.
  */
sealed trait BoardView {
  def squareSize: Int
  def boardController: BoardController

  def toggleHover(hover: Boolean): Unit
  def highlight(location: Location): Unit
  def resetBoard(): Unit
}

case class DefaultBoardView(boardController: BoardController) extends GridPane with BoardView {
  // TODO: Improve this number (e.g. make it dynamically generated)
  override val squareSize = 51

  val canvas = new Canvas(Board.Size * squareSize, Board.Size * squareSize)
  val checkMateDialog = new CheckMateDialog

  init()

  def init() {
    addRow(0, createRanksPane, canvas)
    add(createFilesPane, 1, 1)

    setPadding(new Insets(20))
    setStyle("-fx-background-color: white")

    resetBoard()

    def createRanksPane: GridPane =
      (8 to 1 by -1).foldLeft(new GridPane) { (ranksPane, rank) =>
        val textPane = new BorderPane
        textPane.setMinSize(squareSize / 2, squareSize)
        textPane.setPadding(new Insets(0, 16, 0, 15))

        val text = new Text(String.valueOf(rank))
        text.setTextAlignment(TextAlignment.CENTER)
        text.setFill(Color.GRAY)
        text.setFont(Font.font(18))

        textPane.setCenter(text)
        ranksPane.addColumn(0, textPane)
        ranksPane
      }

    def createFilesPane = "ABCDEFGH".foldLeft(new GridPane) { (filesPane, label) =>
      val textPane = new TextFlow()
      textPane.setMinSize(squareSize, squareSize / 2)
      textPane.setTextAlignment(TextAlignment.CENTER)
      textPane.setPadding(new Insets(15, 0, 15, 0))

      val text = new Text(String.valueOf(label))
      text.setFill(Color.GRAY)
      text.setFont(Font.font(18))

      textPane.getChildren.add(text)
      filesPane.addRow(0, textPane)
      filesPane
    }

    // register events
    val hoverEventHandler = PieceHoverEventHandler(this)
    canvas.addEventHandler(MouseEvent.MOUSE_MOVED, hoverEventHandler)
    canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, MoveEventHandler(this, hoverEventHandler))
  }

  def resetBoard(gc: GraphicsContext): Unit = {
    gc.setFill(Color.WHITE)

    for (row <- 0 until Board.Size) {
      for (col <- 0 until Board.Size) {
        if (col != 0) gc.setFill {
          if (gc.getFill == Color.WHITE) Color.DARKGRAY
          else Color.WHITE
        }
        val x = col * squareSize
        val y = row * squareSize
        gc.fillRect(x, y, squareSize, squareSize)
        boardController.boardAccessor(row, col).foreach(drawPiece(gc, x, y))
      }
    }
  }

  override def resetBoard(): Unit = resetBoard(canvas.getGraphicsContext2D)

  override def toggleHover(hover: Boolean): Unit = getScene.setCursor {
    if (hover) Cursor.HAND else Cursor.DEFAULT
  }

  override def highlight(location: Location): Unit = {
    val gc = canvas.getGraphicsContext2D
    resetBoard(gc)

    val row = Board.Size - 1 - location.rank
    val col: Int = location.file

    gc.setLineWidth(2.5)
    gc.strokeRect(col * squareSize, row * squareSize, squareSize, squareSize)
  }

  def drawPiece(gc: GraphicsContext, x: Int, y: Int)(piece: Piece): Unit = {
    val pieceImage = new Image(Resources.piecePathOf(piece))
    val offsetX = (squareSize - pieceImage.getWidth) / 2
    val offsetY = (squareSize - pieceImage.getHeight) / 2
    gc.drawImage(pieceImage, x + offsetX, y + offsetY)
  }
}


