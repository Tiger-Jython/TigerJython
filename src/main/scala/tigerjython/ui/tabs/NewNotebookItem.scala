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
import javafx.scene.shape.{Circle, Line, Rectangle}
import tigerjython.ui.TigerJythonApplication
import tigerjython.ui.notebook.NotebookTab

/**
 * @author Tobias Kohn
 */
class NewNotebookItem(val parentFrame: OpenDocumentTab) extends DocumentItem {

  {
    titleLabel.setText("New Notebook")
    descriptionLabel.setText("Create a new empty notebook")
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
    for (i <- 1 to 19) {
      val l = new Line(1, 3 * i, 41, 3 * i)
      l.setStroke(Color.SILVER)
      g.getChildren.add(l)
    }
    for (i <- 1 to 13) {
      val l = new Line(3 * i, 1, 3 * i, 58)
      l.setStroke(Color.SILVER)
      g.getChildren.add(l)
    }
    for (i <- 0 to 3) {
      val c = new Circle(5, 16 * i + 6, 1)
      c.setStroke(Color.GRAY)
      g.getChildren.add(c)
    }
    g
  }

  def onClicked(): Unit = {
    tigerjython.execute.TigerJythonProcess.preStart()
    TigerJythonApplication.tabManager.addTab(NotebookTab())
  }

  override def onMouseEnter(): Unit =
    parentFrame.setPreviewText("")

  override def onMouseLeave(): Unit = {}
}
