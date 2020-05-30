/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.tabs

import javafx.scene.{Group, Node}
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{BorderPane, VBox}
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle

/**
 * @author Tobias Kohn
 */
abstract class DocumentItem extends BorderPane {

  protected val icon: Node = createIcon()
  protected val titleLabel = new Label("Hello World!")
  protected val descriptionLabel = new Label()

  {
    this.getStyleClass.add("document-item")
    titleLabel.getStyleClass.add("title")
    descriptionLabel.getStyleClass.add("description")
    if (icon != null)
      icon.getStyleClass.add("icon")
    val contents = new VBox()
    contents.getChildren.addAll(
      titleLabel, descriptionLabel
    )
    setCenter(contents)
    if (icon != null)
    setLeft(icon)

    setMinHeight(60)
  }

  addEventFilter(MouseEvent.MOUSE_CLICKED, (event: MouseEvent) => {
    onClicked()
  })

  protected def createIcon(): Node = {
    val result = new Rectangle(42, 59)
    result.setStroke(Color.BLACK)
    result.getStyleClass.add("paper")
    val outline = new Rectangle(60, 59)
    outline.setFill(Color.TRANSPARENT)
    val g = new Group()
    g.getChildren.add(outline)
    g.getChildren.add(result)
    g
  }

  def onClicked(): Unit
}
