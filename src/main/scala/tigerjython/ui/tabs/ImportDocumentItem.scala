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
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import tigerjython.core.Configuration
import tigerjython.files.Documents
import tigerjython.ui.{TigerJythonApplication, editor}

/**
 * @author Tobias Kohn
 */
class ImportDocumentItem(val parentFrame: OpenDocumentTab) extends DocumentItem {

  {
    titleLabel.setText("Import/Open Document")
    descriptionLabel.setText("Load a document from a file on disc")
  }

  protected lazy val fileChooser: FileChooser = {
    val result = new FileChooser()
    result.setInitialDirectory(Configuration.userHome.toFile)
    result.getExtensionFilters.addAll(
      new ExtensionFilter("Python Files", "*.py"),
      new ExtensionFilter("All Files", "*.*")
    )
    result
  }

  def onClicked(): Unit = {
    val selectedFile = fileChooser.showOpenDialog(TigerJythonApplication.mainStage)
    if (selectedFile != null) {
      val tab = editor.PythonEditorTab()
      tab.loadDocument(Documents.importDocument(selectedFile))
      TigerJythonApplication.tabManager.addTab(tab)
    }
  }

  override def onMouseEnter(): Unit =
    parentFrame.setPreviewText("")

  override def onMouseLeave(): Unit = {}

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
    g.getChildren.addAll(
      new Line(12, 36, 12, 34),
      new Line(30, 36, 30, 34),
      new Line(12, 36, 30, 36),
      new Line(21, 32, 21, 22),
      new Line(21, 22, 17, 26),
      new Line(21, 22, 25, 26)
    )
    g
  }
}
