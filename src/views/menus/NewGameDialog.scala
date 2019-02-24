package views.menus

import javafx.geometry.Insets
import javafx.scene.control._
import javafx.scene.layout.{Border, BorderStroke, VBox}

import engine.board.{Black, Side, White}
import engine.search.AlphaBeta
import models.{GameType, HumanVsComputer, HumanVsHuman, Preferences}
import views.misc.CustomTitledPane

/**
  * Created by melvic on 9/15/18.
  */
class NewGameDialog extends Dialog[ButtonType] {
  val whiteRB = new RadioButton("White")
  val blackRB = new RadioButton("Black")

  val humanVsHumanRB = new RadioButton("Human vs Human")
  val humanVsComputerRB = new RadioButton("Human vs Computer")

  setTitle("New Game")

  lazy val levelPane = {
    val levelPane = new Slider(1,
      AlphaBeta.DefaultMaxDepth,
      AlphaBeta.DefaultMaxDepth / 2 + 1)

    levelPane.setShowTickMarks(true)
    levelPane.setShowTickLabels(true)
    levelPane.setMajorTickUnit(1)
    levelPane.setMinorTickCount(0)
    levelPane.setSnapToTicks(true)

    levelPane
  }

  getDialogPane.setContent {
    val contentPane = new VBox
    contentPane.getChildren.addAll(gameTypePane, sideToPlayPane)
    contentPane.setPadding(new Insets(20))
    contentPane.setSpacing(20)

    def gameTypePane = {
      val mainPane = new VBox()

      val newGameGroup = new ToggleGroup()
      humanVsHumanRB.setToggleGroup(newGameGroup)
      humanVsComputerRB.setToggleGroup(newGameGroup)

      humanVsHumanRB.setSelected(Preferences.Defaults.gameType == HumanVsHuman)

      mainPane.getChildren.addAll(humanVsHumanRB, humanVsComputerRB, levelPane)

      mainPane.setSpacing(15)

      CustomTitledPane("Type of Game", mainPane)
    }

    def sideToPlayPane = {
      val mainPane = new VBox()

      val newGameGroup = new ToggleGroup()
      whiteRB.setToggleGroup(newGameGroup)
      blackRB.setToggleGroup(newGameGroup)

      whiteRB.setSelected(Preferences.Defaults.sideToPlay == White)

      mainPane.getChildren.addAll(whiteRB, blackRB)

      mainPane.setSpacing(15)

      CustomTitledPane("Side to Play", mainPane)
    }

    contentPane
  }

  getDialogPane.getButtonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

  def sideToPlay: Side = if (whiteRB.isSelected) White else Black

  def gameType: GameType =
    if (humanVsHumanRB.isSelected) HumanVsHuman
    else HumanVsComputer(sideToPlay, level)

  def level = levelPane.getValue.toInt
}
