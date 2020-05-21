/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.plugins

import javafx.scene.control.{Alert, ButtonType, TextInputDialog}
import javafx.scene.control.Alert.AlertType
import tigerjython.ui.editor.EditorTab
import tigerjython.ui.{MenuManager, TabManager}

/**
 * The `MainWindow` class is actually only an interface to allow plugins to access the general user interface.
 *
 * @author Tobias Kohn
 */
class MainWindow(val menuManager: MenuManager,
                 val tabManager: TabManager) {

  def addMenuItem(name: String, caption: String, action: Runnable): Unit =
    menuManager.addMenuItem(name, caption, action)

  def alert(message: String): Unit =
    FXExecutor[Unit](() => {
      val alert = new Alert(AlertType.NONE, message, ButtonType.OK)
      alert.showAndWait()
    })

  def confirm(message: String): Boolean =
    FXExecutor[Boolean](() => {
      val alert = new Alert(AlertType.CONFIRMATION, message)
      val result = alert.showAndWait()
      if (result.isPresent)
        result.get() == ButtonType.OK
      else
        false
    })

  def getCaretPosition: Int =
    getCurrentEditor match {
      case Some(editor) =>
        FXExecutor[Int](() => {
          editor.getCaretPosition
        })
      case _ =>
        -1
    }

  def getSelectedText: String =
    getCurrentEditor match {
      case Some(editor) =>
        FXExecutor[String](() => {
          editor.getSelectedText
        })
      case _ =>
        null
    }

  def getText: String =
    getCurrentEditor match {
      case Some(editor) =>
        FXExecutor[String](() => {
          editor.getText
        })
      case _ =>
        null
    }

  def input(prompt: String): String =
    FXExecutor[String](() => {
      val dialog = new TextInputDialog("")
      dialog.setHeaderText(prompt)
      val result = dialog.showAndWait
      if (result.isPresent)
        result.get
      else
        null
    })

  def setSelectedText(s: String): Unit =
    getCurrentEditor match {
      case Some(editor) =>
        FXExecutor[Unit](() => {
          editor.setSelectedText(s)
        })
      case _ =>
    }

  private def getCurrentEditor: Option[EditorTab] =
    tabManager.currentFrame match {
      case Some(editorTab: EditorTab) =>
        Some(editorTab)
      case _ =>
        None
    }
}
