package com.github.melvic.nostalgia.views.misc

import javafx.scene.Node
import javafx.scene.control.TitledPane

/**
  * Created by melvic on 9/15/18.
  */
case class CustomTitledPane(text: String, content: Node) extends TitledPane {
  setText(text)
  setContent(content)
  setCollapsible(false)
}
