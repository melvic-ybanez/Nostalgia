package views.menus

import javafx.beans.property.BooleanProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.geometry.Insets
import javafx.scene.control._
import javafx.scene.layout._

import engine.board.{Black, Side, White}
import engine.search.AlphaBeta
import models._
import views.misc.CustomTitledPane

/**
  * Created by melvic on 9/15/18.
  */
class NewGameDialog extends Dialog[ButtonType] {
  val whiteRB = new RadioButton("White")
  val blackRB = new RadioButton("Black")

  val humanVsHumanRB = new RadioButton("Human vs Human")
  val humanVsComputerRB = new RadioButton("Human vs Computer")
  val computerVsComputerRB = new RadioButton("Computer vs Computer")

  private val hvcLevelPane = levelPane(humanVsComputerRB)
  private val cvcWhiteLevelPane = levelPane(computerVsComputerRB)
  private val cvcBlackLevelPane = levelPane(computerVsComputerRB)

  setTitle("New Game")

  getDialogPane.setContent {
    val contentPane = new VBox
    contentPane.getChildren.addAll(gameTypePane, sideToPlayPane)
    contentPane.setPadding(new Insets(20))
    contentPane.setSpacing(20)

    def gameTypePane = {
      val mainPane = new GridPane()

      val gameTypeTG = new ToggleGroup()
      humanVsHumanRB.setToggleGroup(gameTypeTG)
      humanVsComputerRB.setToggleGroup(gameTypeTG)
      computerVsComputerRB.setToggleGroup(gameTypeTG)

      humanVsHumanRB.setSelected(Preferences.Defaults.gameType == HumanVsHuman)

      mainPane.addRow(0, humanVsHumanRB)

      mainPane.addRow(1, humanVsComputerRB)
      mainPane.addRow(2, levelPaneWrapper(hvcLevelPane, "Level"))

      mainPane.addRow(3, computerVsComputerRB)
      val cvcLevelPaneWrapper = new FlowPane()
      cvcLevelPaneWrapper.getChildren.addAll(
        levelPaneWrapper(cvcWhiteLevelPane, "White's Level"),
        levelPaneWrapper(cvcBlackLevelPane, "Black's Level"))
      mainPane.addRow(4, cvcLevelPaneWrapper)

      mainPane.setHgap(15)
      mainPane.setVgap(15)

      CustomTitledPane("Type of Game", mainPane)
    }

    def sideToPlayPane = {
      val contentPane = new VBox()

      val newGameGroup = new ToggleGroup()
      whiteRB.setToggleGroup(newGameGroup)
      blackRB.setToggleGroup(newGameGroup)

      whiteRB.setSelected(Preferences.Defaults.sideToPlay == White)

      contentPane.getChildren.addAll(whiteRB, blackRB)
      contentPane.setSpacing(15)

      val sideToPlayPane = CustomTitledPane("Side to Play", contentPane)
      sideToPlayPane.disableProperty.bind(computerVsComputerRB.selectedProperty())
      sideToPlayPane
    }

    contentPane
  }

  getDialogPane.getButtonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

  def sideToPlay: Side = if (whiteRB.isSelected) White else Black

  def gameType: GameType =
    if (humanVsHumanRB.isSelected) HumanVsHuman
    else if (humanVsComputerRB.isSelected) HumanVsComputer(sideToPlay, hvcLevelPane.getValue)
    else ComputerVsComputer(cvcWhiteLevelPane.getValue, cvcBlackLevelPane.getValue)

  def levelPane(selection: RadioButton) = {
    val levelPane = new Spinner[Int](1,
      AlphaBeta.DefaultMaxDepth,
      AlphaBeta.DefaultMaxDepth / 2 + 1)
    levelPane.setPrefWidth(60)
    levelPane.disableProperty().bind(selection.selectedProperty().not())
    levelPane
  }

  def levelPaneWrapper(levelPane: Spinner[Int], caption: String) = {
    val wrapper = new FlowPane()
    wrapper.setHgap(10)
    wrapper.setPadding(new Insets(0, 0, 0, 22))

    val label = new Label(caption)
    wrapper.setPrefWidth(180)

    wrapper.getChildren.addAll(new Label(caption + ":"), levelPane)
    wrapper.disableProperty().bind(levelPane.disableProperty)
    wrapper
  }
}
