/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.editor

import javafx.application.Platform
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.Scene
import javafx.scene.control.{TextField, ToolBar}
import javafx.scene.input.KeyCode

/**
 * A simple bar that lets you enter something to be found in the document.
 */
class FindBar(val editorTab: EditorTab) extends ToolBar {

  private val editor = editorTab.editor
  val findTextField: TextField = new TextField()

  private var startIndex: Int = 0
  private var endIndex: Int = 0
  private var currentIndex: Int = 0
  private var searchText: String = _

  {
    getItems.add(findTextField)
    setPrefHeight(48)
    setMinHeight(32)
    setPrefWidth(300)
  }

  findTextField.sceneProperty().addListener(new ChangeListener[Scene] {
    override def changed(observableValue: ObservableValue[_ <: Scene], oldValue: Scene, newValue: Scene): Unit = {
      if (oldValue != newValue && newValue != null)
        findTextField.requestFocus()
    }
  })

  findTextField.setOnKeyPressed(keyEvent => {
    keyEvent.getCode match {
      case KeyCode.ENTER =>
        findNext()
      case KeyCode.ESCAPE =>
        editorTab.hideAuxPanels()
      case _ =>
    }
  })

  findTextField.textProperty().addListener(new ChangeListener[String] {
    override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
      if (oldValue != newValue)
        findFirst(newValue)
    }
  })

  def activate(): Unit = {
    Platform.runLater(() => {
      this.requestFocus()
      val selRange = editor.selectionProperty().getValue
      if (selRange.getLength > 0) {
        startIndex = selRange.getStart
        endIndex = selRange.getEnd
      } else {
        startIndex = 0
        endIndex = -1
      }
      findTextField.setText("")
      findTextField.requestFocus()
    })
  }

  private def doFindText(start: Int): Boolean = {
    if (searchText != null && searchText != "") {
      val s: String = editor.textProperty().getValue
      val index = s.indexOf(searchText, start)
      if (index >= start) {
        editor.selectRange(index, index + searchText.length)
        currentIndex = index
        return true
      }
    }
    if (endIndex > startIndex)
      editor.selectRange(startIndex, endIndex)
    else
      editor.selectRange(startIndex, startIndex)
    currentIndex = -1
    false
  }

  protected def findFirst(searchText: String): Unit = {
    if (this.searchText == null || !searchText.startsWith(this.searchText))
      currentIndex = startIndex
    this.searchText = searchText
    doFindText(currentIndex)
  }

  protected def findNext(): Unit =
    if (currentIndex >= 0) {
      if (!doFindText(currentIndex + 1))
        doFindText(startIndex)
    }
}
