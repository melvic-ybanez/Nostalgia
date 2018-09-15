package views.menus

import javafx.geometry.Insets
import javafx.scene.control.{ButtonType, Dialog, RadioButton, ToggleGroup}
import javafx.scene.layout.VBox

import engine.board.{Black, Side, White}
import models.{HumanVsHuman, Preferences}
import views.misc.CustomTitledPane

/**
  * Created by melvic on 9/15/18.
  */
class NewGameDialog extends Dialog[ButtonType] {
  val whiteRB = new RadioButton("White")
  val blackRB = new RadioButton("Black")

  setTitle("New Game")

  val contentPane = {
    val contentPane = new VBox
    contentPane.getChildren.addAll(createGameTypePane, createSideToPlayPane)
    contentPane.setPadding(new Insets(20))
    contentPane.setSpacing(20)

    def createGameTypePane = {
      val mainPane = new VBox()

      val humanVsHumanRB = new RadioButton("Human vs Human")
      val humanVsComputerRB = new RadioButton("Human vs Computer")

      val newGameGroup = new ToggleGroup()
      humanVsHumanRB.setToggleGroup(newGameGroup)
      humanVsComputerRB.setToggleGroup(newGameGroup)

      humanVsHumanRB.setSelected(Preferences.Defaults.gameType == HumanVsHuman)

      mainPane.getChildren.addAll(humanVsHumanRB, humanVsComputerRB)

      mainPane.setSpacing(15)

      CustomTitledPane("Type of Game", mainPane)
    }

    def createSideToPlayPane = {
      val mainPane = new VBox()

      val newGameGroup = new ToggleGroup()
      whiteRB.setToggleGroup(newGameGroup)
      blackRB.setToggleGroup(newGameGroup)

      whiteRB.setSelected(Preferences.Defaults.sideToPlay == White)

      mainPane.getChildren.addAll(whiteRB, blackRB)

      mainPane.setSpacing(15)

      CustomTitledPane("Side to Play", mainPane)
    }
  }

  def sideToPlay: Side = if (whiteRB.isSelected) White else Black
}
