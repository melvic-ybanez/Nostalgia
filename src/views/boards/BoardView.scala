package views.boards

import javafx.animation.{Interpolator, KeyFrame, KeyValue, Timeline}
import javafx.beans.property.{DoubleProperty, SimpleDoubleProperty}
import javafx.beans.value.{WritableDoubleValue, WritableValue}
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.control.{Alert, ButtonType}
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{BorderPane, GridPane}
import javafx.scene.paint.Color
import javafx.scene.text.{Font, Text, TextAlignment, TextFlow}
import javafx.util.Duration

import controllers.BoardController
import engine.board.{Board, Piece, Side}
import engine.movegen.{E, Location, Move, _4}
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

  val hoverEventHandler = PieceHoverEventHandler(this)
  val moveEventHandler = MoveEventHandler(this, hoverEventHandler)

  init()

  def init() {
    setPadding(new Insets(20))
    setStyle("-fx-background-color: white")

    resetBoard()

    // register events
    canvas.addEventHandler(MouseEvent.MOUSE_MOVED, hoverEventHandler)
    canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, moveEventHandler)
  }

  def createRanksPane: GridPane = {
    val ranks =
      if (boardController.boardAccessor.isRotated) 1 to 8 by 1
      else 8 to 1 by -1

    ranks.foldLeft(new GridPane) { (ranksPane, rank) =>
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
  }

  def createFilesPane = {
    val files = {
      val files = "ABCDEFGH"
      if (boardController.boardAccessor.isRotated) files.reverse
      else files
    }

    files.foldLeft(new GridPane) { (filesPane, label) =>
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
  }

  def resetBoard(gc: GraphicsContext): Unit = {
    drawBoard(gc)
    getChildren.clear()
    addRow(0, createRanksPane, canvas)
    add(createFilesPane, 1, 1)
  }

  def drawBoard(gc: GraphicsContext): Unit = {
    gc.setFill(Color.WHITE)

    // draw the board
    for (row <- 0 until Board.Size) {
      for (col <- 0 until Board.Size) {
        if (col != 0) gc.setFill {
          if (gc.getFill == Color.WHITE) Color.DARKGRAY
          else Color.WHITE
        }
        val x = col * squareSize
        val y = row * squareSize
        gc.fillRect(x, y, squareSize, squareSize)

        val isMovingPiece = boardController.boardAccessor.board.lastMove.exists {
          case Move(source, destination, _) =>
            val location = Location.locate(row, col)
            location == source || location == destination && {
              val movingPiece = boardController.boardAccessor.board(destination)
              movingPiece.get.side == boardController.sideToMove
            }
          case _ => false
        }

        if (!isMovingPiece)
          boardController.boardAccessor(row, col).foreach(drawPiece(gc, x, y))
      }
    }
  }

  def animateMove(gc: GraphicsContext = canvas.getGraphicsContext2D): Unit = {
    // animate the moving piece
    boardController.boardAccessor.board.lastMove.foreach { lastMove =>
      val netMove = boardController.boardAccessor.accessorMove(lastMove)

      // make the coordinates compatible with the board view's
      // because the direction of the ranks are reversed
      val source = Location.locate(netMove.source.rank, netMove.source.file)
      val dest = Location.locate(netMove.destination.rank, netMove.destination.file)

      val animator = new MoveAnimator(this) {
        override def handle(now: Long, x: DoubleProperty, y: DoubleProperty) = {
          val piece = boardController.boardAccessor.board(lastMove.destination).get
          drawBoard(gc)
          drawPiece(gc, x.intValue, y.intValue)(piece)
        }
      }
      animator.animate(gc, source.file, source.rank, dest.file, dest.rank)
    }
  }

  def resetEventHandlers(): Unit = {
    hoverEventHandler.reset()
    moveEventHandler.reset()
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

  def showCheckmateDialog(winningSide: Side): Unit = {
    val checkMateAlert = new Alert(Alert.AlertType.CONFIRMATION)
    checkMateAlert.setHeaderText(null)
    checkMateAlert.setTitle("Checkmate")
    checkMateAlert.setContentText(s"$winningSide wins by checkmate. Do you want to start a new game?")
    checkMateAlert.getButtonTypes.setAll(ButtonType.NO, ButtonType.YES)
    checkMateAlert.showAndWait().ifPresent { result =>
      if (result == ButtonType.YES) boardController.menuController.newGame()
    }
  }
}


