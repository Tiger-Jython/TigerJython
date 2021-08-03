/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.preferences

import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.scene.Node
import javafx.scene.control.TextArea
import javafx.scene.layout.{StackPane, VBox}
import tigerjython.core.{BuildInfo, Configuration}
import tigerjython.execute.PythonInstallations
import tigerjython.ui.Utils.onFX
import tigerjython.ui.{UIString, ZoomMixin}
import tigerjython.utils.OSPlatform

/**
 * This pane does not provide preferences or settings as such, but rather an overview of the system status and logs for
 * bug reports and diagnosis.
 *
 * @author Tobias Kohn
 */
class StatusMessagesPane extends PreferencePane {

  val caption: StringProperty = new SimpleStringProperty("Messages")
  UIString("prefs.messages") += caption

  protected val textArea: TextArea = createTextArea

  protected def createTextArea: TextArea = {
    val result = new TextArea() with ZoomMixin
    result.setEditable(false)
    result.setWrapText(false)
    result.getStyleClass.add("status-pane")
    result.setText(createInitialText)
    result
  }

  protected def createInitialText: String =
    "TigerJython %s\n  on %s\n  on %s\nAvailable Python Interpreters:\n  %s\n---".format(
      BuildInfo.fullVersion,
      Configuration.getFullJavaVersion,
      OSPlatform.getFullSystemName,
      PythonInstallations.getAvailableVersions.mkString("; ")
    )

  override lazy val node: Node = {
    val result = new VBox()
    result.getChildren.add(textArea)
    new StackPane(result)
  }

  def append(s: String): Unit = onFX(() => {
    textArea.appendText(s)
  })

  def clear(): Unit = onFX(() => {
    textArea.clear()
  })

  def getContentText: String =
    textArea.getText()
}
