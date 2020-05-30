/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.tabs

import javafx.scene.layout.VBox
import tigerjython.files.Documents
import tigerjython.ui.TabFrame

/**
 * @author Tobias Kohn
 */
class OpenDocumentTab protected () extends TabFrame {

  caption.setValue("+")

  override def focusChanged(receiveFocus: Boolean): Unit =
    if (receiveFocus)
      update()

  def update(): Unit = {
    getChildren.clear()
    val box = new VBox()
    box.getChildren.add(new NewDocumentItem())
    for (doc <- Documents.getListOfDocuments)
      box.getChildren.add(new OpenDocumentItem(doc))
    getChildren.add(box)
  }
}
object OpenDocumentTab {

  private lazy val _openDocTab: OpenDocumentTab = new OpenDocumentTab()

  def apply(): OpenDocumentTab = _openDocTab
}
