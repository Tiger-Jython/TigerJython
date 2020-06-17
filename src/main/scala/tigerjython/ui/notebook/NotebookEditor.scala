/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.notebook

import java.lang

import javafx.application.Platform
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.text.{Font, Text}
import tigerjython.core.Preferences
import tigerjython.ui.editing._

/**
 * @author Tobias Kohn
 */
class NotebookEditor(val cell: NotebookCell) extends PythonCodeArea {

  private var _linesCount: Int = 0
  private var _linesHeight: Double = 17.0

  {
    setPrefHeight(_linesHeight + 1)
  }

  def setTextSize(size: Double): Unit = {
    setStyle("-fx-font-size: %g; -fx-font-family: \"%s\";".format(
      size,
      Preferences.fontFamily.get
    ))
    val txt = new Text("Xyq")
    txt.setFont(new Font(Preferences.fontFamily.get, size))
    _linesHeight = txt.getLayoutBounds.getHeight
    val lines = _linesCount max 1
    Platform.runLater(() => {
      setPrefHeight(lines * _linesHeight + 1)
    })
  }

  def recomputeHeight(): Unit = {
    val lines = _linesCount max 1
    lookup(".text") match {
      case txt: Text =>
        _linesHeight = txt.getBoundsInLocal.getHeight max 8.0
      case _ =>
    }
    setPrefHeight(lines * _linesHeight + 1)
  }

  protected def setLinesCount(lines: Int): Unit =
    if (lines != _linesCount) {
      _linesCount = lines
      recomputeHeight()
    }

  textProperty().addListener(new ChangeListener[String] {
    override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
      val lines = newValue.count(_ == '\n') + 1
      setLinesCount(lines)
    }
  })

  focusedProperty().addListener(new ChangeListener[lang.Boolean] {
    override def changed(observableValue: ObservableValue[_ <: lang.Boolean], t: lang.Boolean, focused: lang.Boolean): Unit = {

    }
  })
}
