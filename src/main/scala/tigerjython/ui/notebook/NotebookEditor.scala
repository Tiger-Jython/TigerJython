/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.notebook

import java.lang
import java.util.regex.Pattern

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.text.Text
import org.fxmisc.richtext.CodeArea
import tigerjython.core.Preferences

/**
 * @author Tobias Kohn
 */
class NotebookEditor(val cell: NotebookCell) extends CodeArea {

  private var _linesCount: Int = 0
  private var _linesHeight: Double = 17.0

  {
    setStyle("-fx-font-family: \"%s\";".format(Preferences.fontFamily.get))
    setPrefHeight(_linesHeight + 1)
  }

  protected def setLinesCount(lines: Int): Unit =
    if (lines != _linesCount) {
      lookup(".text") match {
        case txt: Text =>
          _linesHeight = txt.getBoundsInLocal.getHeight
        case _ =>
      }
      setPrefHeight(lines * _linesHeight + 1)
      _linesCount = lines
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
object NotebookEditor {

  private val KEYWORDS = Array(
    "and", "as", "assert", "async", "await", "break", "class", "continue", "def", "del", "elif", "else",
    "except", "finally", "for", "from", "global", "if", "import", "in", "is", "lambda", "nonlocal", "not",
    "or", "pass", "raise", "return", "try", "while", "with", "yield",
    "False", "None", "True"
  )

  private val COMMENT_PATTERN = "#[^\\n]*"
  private val KEYWORD_PATTERN = "\\b(" + KEYWORDS.mkString("|") + "|repeat)\\b"
  private val KEYWORD_PATTERN_PLAIN = "\\b(" + KEYWORDS.mkString("|") + ")\\b"
  private val STRING_PATTERN = "\\\"([^\\\"\\\\\\\\]|\\\\\\\\.)*\\\""

  private lazy val PATTERN = Pattern.compile(
    "(?<KEYWORD>" + KEYWORD_PATTERN + ")" +
      "|(?<STRING>" + STRING_PATTERN + ")" +
      "|(?<COMMENT>" + COMMENT_PATTERN + ")"
  )

  private lazy val PATTERN_PLAIN = Pattern.compile(
    "(?<KEYWORD>" + KEYWORD_PATTERN_PLAIN + ")" +
      "|(?<STRING>" + STRING_PATTERN + ")" +
      "|(?<COMMENT>" + COMMENT_PATTERN + ")"
  )
}