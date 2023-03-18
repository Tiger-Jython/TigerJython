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
import javafx.scene.{Group, Node, Scene}
import javafx.scene.control.{Button, Label, TextField, ToolBar}
import javafx.scene.input.KeyCode
import javafx.scene.shape.{Line, StrokeLineCap}

import java.lang

/**
 * A simple bar that lets you enter something to be found in the document.
 */
class FindBar(val editorTab: EditorTab) extends ToolBar {

  private val editor = editorTab.editor
  val findTextField: TextField = new TextField()
  private val countLabel: Label = new Label()
  private val findNextButton = new Button()
  private val findPrevButton = new Button()

  private var startPos: Int = 0
  private var endPos: Int = 0

  private var currentIndex: Int = 0
  private var searchText: String = _
  private var fullText: String = _
  private var fullTextLC: String = _

  private var occurrences: Array[Int] = _

  {
    getItems.addAll(findTextField, findNextButton, findPrevButton, countLabel)
    setPrefHeight(48)
    setMinHeight(32)
    findTextField.setPrefWidth(300)
    findTextField.prefWidthProperty().bind(this.widthProperty().divide(3))
    findNextButton.setGraphic(createDownImage)
    findNextButton.getStyleClass.add("download-text-btn")
    findNextButton.setOnAction(_ => findNext())
    findNextButton.setFocusTraversable(false)
    findPrevButton.setGraphic(createUpImage)
    findPrevButton.getStyleClass.add("download-text-btn")
    findPrevButton.setOnAction(_ => findPrev())
    findPrevButton.setFocusTraversable(false)
  }

  focusedProperty().addListener(new ChangeListener[java.lang.Boolean] {
    override def changed(observableValue: ObservableValue[_ <: lang.Boolean], oldValue: lang.Boolean, newValue: lang.Boolean): Unit = {
      if (newValue && !oldValue) {
        updateText()
      }
    }
  })

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

  private def createDownImage: Node = {
    val result = new Group()
    val lines = Array(
      new Line(-4, -2, 0, 2),
      new Line(0, 2, 4, -2),
    )
    for (line <- lines)
      line.setStrokeLineCap(StrokeLineCap.ROUND)
    result.getChildren.addAll(lines: _*)
    result
  }

  private def createUpImage: Node = {
    val result = new Group()
    val lines = Array(
      new Line(-4, 2, 0, -2),
      new Line(0, -2, 4, 2),
    )
    for (line <- lines)
      line.setStrokeLineCap(StrokeLineCap.ROUND)
    result.getChildren.addAll(lines: _*)
    result
  }

  def activate(): Unit = {
    Platform.runLater(() => {
      val selRange = editor.selectionProperty().getValue
      if (selRange.getLength > 0) {
        startPos = selRange.getStart
        endPos = selRange.getEnd
      } else {
        startPos = 0
        endPos = -1
      }
      fullText = null
      updateText()
      findTextField.setText("")
      findTextField.requestFocus()
    })
  }

  private def hasUppercase(s: String): Boolean = {
    for (ch <- s)
      if (ch.isUpper)
        return true
    false
  }

  private def doFindAll(): Array[Int] =
    if (searchText != null && searchText != "") {
      val s: String = if (hasUppercase(searchText)) fullText else fullTextLC
      val result = collection.mutable.ArrayBuffer[Int]()
      val lastIndex =
        if (endPos == -1)
          s.length - 1
        else
          endPos - searchText.length
      var idx = s.indexOf(searchText, startPos)
      while (startPos <= idx && idx <= lastIndex) {
        result += idx
        idx = s.indexOf(searchText, idx + 1)
      }
      result.toArray
    } else
      Array()

  private def selectEntry(idx: Int): Unit =
    if (idx < 0 && occurrences.nonEmpty)
      selectEntry(idx + occurrences.length)
    else if (occurrences.nonEmpty && searchText != "") {
      currentIndex =
        if (occurrences.length > 1)
          idx % occurrences.length
        else
          0
      val pos = occurrences(currentIndex)
      editor.selectRange(pos, pos + searchText.length)
      countLabel.setText("%d/%d".format(currentIndex + 1, occurrences.length))
    } else {
      if (endPos > startPos)
        editor.selectRange(startPos, endPos)
      else
        editor.selectRange(startPos, startPos)
      countLabel.setText("0/0")
      currentIndex = -1
    }

  private def nextIndexAfterPos: Int = {
    val curPos = editor.selectionProperty().getValue.getStart
    for ((pos, i) <- occurrences.zipWithIndex)
      if (curPos <= pos)
        return i
    0
  }

  protected def findFirst(searchText: String): Unit = {
    if (this.searchText == null || !searchText.startsWith(this.searchText))
      currentIndex = startPos
    this.searchText = searchText
    occurrences = doFindAll()
    if (occurrences.nonEmpty)
      selectEntry(nextIndexAfterPos)
    else
      selectEntry(0)
  }

  protected def findNext(): Unit = {
    selectEntry(currentIndex + 1)
  }

  protected def findPrev(): Unit = {
    selectEntry(currentIndex - 1)
  }

  protected def updateText(): Unit = {
    fullText = editor.textProperty().getValue
    fullTextLC = fullText.toLowerCase
  }
}
