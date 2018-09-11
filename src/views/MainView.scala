package views

import javafx.scene.layout.BorderPane

/**
  * Created by melvic on 9/12/18.
  */
class MainView extends BorderPane {
  setCenter(new DefaultBoardView(null))
}
