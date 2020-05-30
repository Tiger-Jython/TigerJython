/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.tabs

import javafx.scene.{Group, Node}
import javafx.scene.paint.Color
import javafx.scene.shape.{Line, Rectangle}
import tigerjython.files.Document

/**
 * @author Tobias Kohn
 */
class OpenDocumentItem(val document: Document) extends DocumentItem {

  {
    titleLabel.textProperty().bind(document.name)
    descriptionLabel.textProperty().bind(document.description)
  }

  override protected def createIcon(): Node = {
    val result = new Rectangle(42, 59)
    result.setStroke(Color.BLACK)
    result.getStyleClass.add("paper")
    val outline = new Rectangle(60, 59)
    outline.setFill(Color.TRANSPARENT)
    val g = new Group()
    g.getChildren.addAll(
      outline, result
    )
    for (i <- 0 to 13) {
      val l = new Line(10, 10 + 3 * i, 32, 10 + 3 * i)
      l.setStroke(Color.GRAY)
      g.getChildren.add(l)
    }
    g
  }

  def onClicked(): Unit =
    document.show()
}