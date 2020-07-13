package com.github.melvic.nostalgia.views.boards

import com.github.melvic.nostalgia.animations.MoveAnimator
import com.github.melvic.nostalgia.controllers.GameController
import com.github.melvic.nostalgia.engine.board.bitboards.BitboardInstance
import com.github.melvic.nostalgia.engine.board.{Board, Piece, Side, White}
import com.github.melvic.nostalgia.engine.movegen.Location._
import com.github.melvic.nostalgia.engine.movegen.{Location, MMove}
import com.github.melvic.nostalgia.events.{MoveEventHandler, PieceHoverEventHandler}
import com.github.melvic.nostalgia.main.Resources
import com.github.melvic.nostalgia.math.{NCell, NCoordinate, Point}
import javafx.application.Platform
import javafx.beans.property.DoubleProperty
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.control.{Alert, ButtonType}
import javafx.scene.effect.{DropShadow, Effect, Glow}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{BorderPane, GridPane}
import javafx.scene.paint.Color
import javafx.scene.text.{Font, Text, TextAlignment, TextFlow}
import scalafx.scene.effect.InnerShadow

/**
  * Created by melvic on 9/11/18.
  */
trait BoardView {
  def squareSize: Double
  def boardController: GameController

  def toggleHover(hover: Boolean): Unit
  def highlight(location: Location): Unit
  def resetBoard(lowerSide: Side = White, fullReset: Boolean = true): Unit

  def showGameOverDialog(winningSide: Side, reason: String): Unit
  def showResignConfirmationDialog(): Unit

  def animateMove(finished: => Unit): Unit
  def rotate(): Unit

  def registerListeners(): Unit
  def removeListeners(): Unit

  def topCanvasOffset: Double
  def boundedSquareSize: Double = squareSize + topCanvasOffset
}

case class DefaultBoardView(boardController: GameController) extends GridPane with BoardView {
  override val squareSize = NCoordinate[Point].size
  override val topCanvasOffset = NCoordinate[Point].offsets.top

  // TODO: Move this to a stylesheet if it becomes complex enough.
  val DarkColorHex = "#969696"
  val DarkColor = Color.web(DarkColorHex)
  val LightColor = Color.WHITE

  private var _lowerSide: Side = White

  implicit val canvas: Canvas = new Canvas(
    Board.Size * squareSize,
    Board.Size * squareSize + topCanvasOffset)

  val hoverEventHandler = PieceHoverEventHandler(this)
  val moveEventHandler = MoveEventHandler(this, hoverEventHandler)

  init()

  def init() {
    setPadding(new Insets(25, 30, 0, 20))
    setStyle(s"-fx-background-color: $DarkColorHex")
    paintAll()
  }

  def createRanksPane: GridPane = {
    val ranks =
      if (boardController.boardAccessor.isRotated) 1 to 8 by 1
      else 8 to 1 by -1

    val ranksPane = ranks.foldLeft(new GridPane) { (ranksPane, rank) =>
      val textPane = new BorderPane
      textPane.setMinSize(squareSize / 2, squareSize)
      textPane.setPadding(new Insets(0, 16, 0, 15))

      val text = new Text(String.valueOf(rank))
      text.setTextAlignment(TextAlignment.CENTER)
      text.setFill(Color.WHITE)
      text.setFont(Font.font(18))

      textPane.setCenter(text)
      ranksPane.addColumn(0, textPane)
      ranksPane
    }
    ranksPane.setPadding(new Insets(topCanvasOffset, 0, 0, 0))
    ranksPane
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
      text.setFill(Color.WHITE)
      text.setFont(Font.font(18))

      textPane.getChildren.add(text)
      filesPane.addRow(0, textPane)
      filesPane
    }
  }

  override def resetBoard(lowerSide: Side, fullReset: Boolean = true): Unit = {
    _lowerSide = lowerSide
    drawBoard(canvas.getGraphicsContext2D, fullReset)
  }

  override def rotate(): Unit = {
    getChildren.clear()
    paintAll()
  }

  def paintAll(): Unit = {
    add(createRanksPane, 0, 0)
    resetBoard(_lowerSide)
    add(canvas, 1, 0)
    add(createFilesPane, 1, 1)
  }

  def drawBoard(gc: GraphicsContext, fullReset: Boolean): Unit = {
    gc.clearRect(0, 0, canvas.getWidth, canvas.getHeight)

    gc.setFill(LightColor)

    // draw the board
    for (row <- 0 until Board.Size) {
      for (col <- 0 until Board.Size) {
        if (col != 0) gc.setFill {
          if (gc.getFill == LightColor) DarkColor else LightColor
        }

        val cell = NCell(col, row)
        val coord = cell.toCoordinate[Point]

        gc.fillRect(coord.x, coord.y, squareSize, squareSize)
        val accessor = boardController.boardAccessor
        val isMovingPiece = !fullReset && accessor.board.lastMove.exists {
          case MMove(source, destination, _) =>
            val location = accessor.accessorLocation(Location.locateForView(row, col))
            location == source || location == destination
          case _ => false
        }

        if (!isMovingPiece)
          boardController.boardAccessor(row, col).foreach(drawPiece(gc, coord.x, coord.y, _))
      }
    }
  }

  override def animateMove(finished: => Unit = ()): Unit = {
    val gc = canvas.getGraphicsContext2D
    val board = boardController.boardAccessor.board

    // animate the moving piece
    board.lastMove.foreach { lastMove =>
      val netMove = boardController.boardAccessor.accessorMove(lastMove)
      val (source, dest) = MMove.locateMove(netMove)

      board(lastMove.destination).foreach { case piece@Piece(_, side) =>
        val animator = new MoveAnimator(this) {
          override def handle(now: Long, x: DoubleProperty, y: DoubleProperty): Unit = {
            drawBoard(gc, false)
            drawPiece(gc, x.doubleValue, y.doubleValue, piece)
          }

          override def beforeFinished(): Unit = {
            drawBoard(gc, true)
            highlight(netMove.destination)
          }

          override def onFinished(event: ActionEvent): Unit = {
            registerListeners()
            Platform.runLater(() => finished)
          }
        }
        animator.animate(gc, source.file, source.rank, dest.file, dest.rank)
      }
    }
  }

  def resetEventHandlers(): Unit = {
    registerListeners()
    hoverEventHandler.reset()
    moveEventHandler.reset()
  }

  override def toggleHover(hover: Boolean): Unit = getScene.setCursor {
    if (hover) Cursor.HAND else Cursor.DEFAULT
  }

  override def highlight(location: Location): Unit = {
    val gc = canvas.getGraphicsContext2D

    val row = Board.Size - 1 - location.rank
    val col: Int = location.file

    val cell = NCell(col, row)
    val coord = cell.toCoordinate[Point]

    drawBoard(gc, fullReset = true)

    // Fill the square
    gc.setFill(Color.STEELBLUE)
    gc.fillRect(coord.x, coord.y, coord.size, coord.size)

    // Add effects to the piece
    val effect = new DropShadow()
    effect.setColor(Color.TURQUOISE)
    gc.setEffect(effect)
    drawPiece(gc, col, row)

    gc.setEffect(null)

    // For every piece not above the selected one, redraw the piece
    // below it in case it exceeds it's own square and overlaps with
    // the piece above
    for (r <- (row + 1) until Board.Size) {
      drawPiece(gc, col, r, clear = true)
    }
  }

  def drawPiece(gc: GraphicsContext, x: Double, y: Double, piece: Piece): Unit = {
    val pieceImage = new Image(Resources.piecePathOf(piece, piece.side != _lowerSide))

    val defaultPadding = 3
    val offsetX = (squareSize - pieceImage.getWidth) / 2
    val offsetY = {
      val height = pieceImage.getHeight
      if (height > squareSize)
        squareSize - height - defaultPadding
      else (squareSize - height) / 2
    }

    gc.drawImage(pieceImage, x + offsetX, y + offsetY)
  }

  def drawPiece(gc: GraphicsContext, col: Int, row: Int, clear: Boolean = false): Unit = {
    val cell  = NCell(col, row)
    val coord = cell.toCoordinate[Point]

    // This is a trick we use to avoid the "bold" image effect
    // brought by repeated drawing of an image. This clears the old image,
    // making the one we are about to draw a fresh object.
    if (clear) {
      gc.setFill(detectSquareColor(gc, col, row))
      gc.fillRect(coord.x, coord.y, squareSize, squareSize)
    }

    boardController.boardAccessor(cell.row, cell.col).foreach(
      drawPiece(gc, coord.x, coord.y, _))
  }

  def detectSquareColor(gc: GraphicsContext, col: Int, row: Int) = {
    val boardColorTable = 0x55aa55aa55aa55aaL
    val squareBitset = 1 << Location.locateForView(row, col).toBitPosition
    if (Bitboard.isEmptySet(boardColorTable & squareBitset)) DarkColor else LightColor
  }

  override def showGameOverDialog(winningSide: Side, reason: String): Unit = {
    val checkMateAlert = new Alert(Alert.AlertType.CONFIRMATION)
    checkMateAlert.setHeaderText(null)
    checkMateAlert.setTitle("Game Over")
    checkMateAlert.setContentText(s"$winningSide wins by $reason. Would you like to start a new game?")
    checkMateAlert.getButtonTypes.setAll(ButtonType.NO, ButtonType.YES)
    checkMateAlert.showAndWait.ifPresent { result =>
      if (result == ButtonType.YES) boardController.menuController.newGame()
    }
  }

  lazy val resignConfirmationDialog = {
    val alert = new Alert(Alert.AlertType.CONFIRMATION)
    alert.setHeaderText(null)
    alert.setTitle("Confirm Resignation")
    alert.setContentText("Are you sure you want to resign?")
    alert.getButtonTypes.setAll(ButtonType.NO, ButtonType.YES)
    alert
  }

  override def showResignConfirmationDialog(): Unit =
    resignConfirmationDialog.showAndWait().ifPresent { result =>
      if (result == ButtonType.YES) {
        boardController.gameOver(boardController.sideToMove.opposite, "resignation")
      }
    }

  override def registerListeners(): Unit = {
    canvas.addEventHandler(MouseEvent.MOUSE_MOVED, hoverEventHandler)
    canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, moveEventHandler)
  }

  override def removeListeners(): Unit = {
    canvas.removeEventHandler(MouseEvent.MOUSE_MOVED, hoverEventHandler)
    canvas.removeEventHandler(MouseEvent.MOUSE_CLICKED, moveEventHandler)
  }
}


