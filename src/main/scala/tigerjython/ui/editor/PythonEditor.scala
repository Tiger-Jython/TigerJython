/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.editor

import java.time.Duration
import java.util.Optional
import java.util.concurrent.Executors
import java.util.regex.Pattern

import javafx.concurrent.Task
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.shape.Rectangle
import org.fxmisc.richtext._
import org.fxmisc.richtext.model.{StyleSpans, StyleSpansBuilder}
import tigerjython.core.Preferences
import tigerjython.plugins.EventManager
import tigerjython.ui.ZoomMixin

/**
 * The Python editor inherits from _RichTextFX_'s `CodeArea` and customises it for highlighting Python code, allowing
 * to zoom in and out of text, or to save the text content after two seconds of no user input.
 *
 * @author Tobias Kohn
 */
class PythonEditor extends CodeArea with ZoomMixin {

  import tigerjython.ui.Utils._

  private val executor = Executors.newSingleThreadExecutor()
  protected val gutterRect = new Rectangle()

  override protected def layoutChildren(): Unit = {
    try {
      val children = getChildren
      if (!(children.get(0) eq gutterRect))
        children.add(0, gutterRect)
      val index = visibleParToAllParIndex(0)
      val wd = getParagraphGraphic(index).prefWidth(-1)
      gutterRect.setWidth(wd)
    } catch {
      case _: Throwable =>
    }
    super.layoutChildren()
  }

  {
    setParagraphGraphicFactory(TJLineNumberFactory.get(this))
    gutterRect.heightProperty.bind(this.heightProperty)
    gutterRect.getStyleClass.add("lineno")

    setStyle("-fx-font-family: \"%s\";".format(Preferences.fontFamily.get))

    addEventFilter(KeyEvent.KEY_TYPED, (key: KeyEvent) => {
      val char = key.getCharacter
      if (char != null && char != KeyEvent.CHAR_UNDEFINED)
        EventManager.fireOnKeyPressed(getCaretPosition, char)
    })
    addEventFilter(KeyEvent.KEY_PRESSED, (key: KeyEvent) =>
      key.getCode match {
        case KeyCode.ENTER =>
          val currentLine = getText(getCurrentParagraph)
          var indentation = currentLine.takeWhile(_ == ' ')
          val column = getCaretColumn
          if (column > 0 && currentLine(column-1) == ':')
            indentation += " " * Preferences.tabWidth.get
          if (indentation != "") {
            val text = "\n" + indentation
            onFX(() => { insertText(getCaretPosition, text) })
            key.consume()
          }
          EventManager.fireOnKeyPressed(getCaretPosition, KeyCode.ENTER.toString)
        case KeyCode.TAB =>
          val tabWidth = Preferences.tabWidth.get
          val x = tabWidth - (getCaretColumn % tabWidth)
          onFX(() => { insertText(getCaretPosition, " " * x) })
          key.consume()
          EventManager.fireOnKeyPressed(getCaretPosition, KeyCode.TAB.toString)
        case KeyCode.BACK_SPACE =>
          val tabWidth = Preferences.tabWidth.get
          val currentLine = getText(getCurrentParagraph)
          val indentation = currentLine.segmentLength(_ == ' ')
          val back_width = {
            val value = indentation % tabWidth
            if (value == 0 && indentation >= tabWidth)
              tabWidth
            else
              value
          }
          if (back_width > 1 && getCaretColumn == indentation) {
            val pos = getCaretPosition
            onFX(() => { replaceText(pos - back_width, pos, "") })
            key.consume()
          }
          EventManager.fireOnKeyPressed(getCaretPosition, KeyCode.BACK_SPACE.toString)
        case KeyCode.DELETE =>
          EventManager.fireOnKeyPressed(getCaretPosition, KeyCode.DELETE.toString)
        case code @ (KeyCode.LEFT | KeyCode.RIGHT | KeyCode.UP | KeyCode.DOWN |
                     KeyCode.PAGE_UP | KeyCode.PAGE_DOWN | KeyCode.HOME | KeyCode.END) =>
          EventManager.fireOnKeyPressed(getCaretPosition, code.toString)
        case _ =>
      }
    )

    val cleanupWhenDone = this.multiPlainChanges()
      .successionEnds(Duration.ofMillis(50))
      .supplyTask(() => computeHighlightingAsync())
      .awaitLatest(this.multiPlainChanges())
      .filterMap((t: org.reactfx.util.Try[StyleSpans[java.util.Collection[String]]]) => {
        val result: java.util.Optional[StyleSpans[java.util.Collection[String]]] =
          if (t.isSuccess)
            Optional.of(t.get)
          else {
            t.getFailure.printStackTrace()
            Optional.empty()
          }
        result
      })
      .subscribe(h => applyHighlighting(h))

    val autoSave = this.multiPlainChanges()
      .successionEnds(Duration.ofMillis(2500))
      .supplyTask(() => autoSaveAsync())
      .awaitLatest(this.multiPlainChanges())
      .observe(_ => {})
  }

  private def computeHighlightingAsync(): Task[StyleSpans[java.util.Collection[String]]] = {
    val text = this.getText()
    val task = new Task[StyleSpans[java.util.Collection[String]]]() {
      override def call(): StyleSpans[java.util.Collection[String]] =
        computeHighlighting(text)
    }
    executor.execute(task)
    task
  }

  private def autoSaveAsync(): Task[Unit] = {
    val task = new Task[Unit]() {
      override def call(): Unit = {
        if (onAutoSave != null)
          onAutoSave()
      }
    }
    executor.execute(task)
    task
  }

  var onAutoSave: ()=>Unit = _

  private def applyHighlighting(highlighting: StyleSpans[java.util.Collection[String]]): Unit = {
    this.setStyleSpans(0, highlighting)
  }

  private def computeHighlighting(text: String): StyleSpans[java.util.Collection[String]] = {
    val matcher = PythonEditor.PATTERN.matcher(text)
    val spansBuilder = new StyleSpansBuilder[java.util.Collection[String]]()
    var lastKwEnd = 0
    while (matcher.find()) {
      val styleClass =
        if (matcher.group("KEYWORD") != null)
          "keyword"
        else if (matcher.group("STRING") != null)
          "string"
        else
          "comment"
      spansBuilder.add(java.util.Collections.emptyList(), matcher.start() - lastKwEnd)
      spansBuilder.add(java.util.Collections.singleton(styleClass), matcher.end() - matcher.start())
      lastKwEnd = matcher.end()
    }
    spansBuilder.add(java.util.Collections.emptyList(), text.length - lastKwEnd)
    spansBuilder.create()
  }
}
object PythonEditor {

  private val KEYWORDS = Array(
    "and", "as", "assert", "async", "await", "break", "class", "continue", "def", "del", "elif", "else",
    "except", "finally", "for", "from", "global", "if", "import", "in", "is", "lambda", "nonlocal", "not",
    "or", "pass", "raise", "return", "try", "while", "with", "yield",
    "False", "None", "True"
  )

  private val COMMENT_PATTERN = "#[^\\n]*"
  private val KEYWORD_PATTERN = "\\b(" + KEYWORDS.mkString("|") + ")\\b"
  private val STRING_PATTERN = "\\\"([^\\\"\\\\\\\\]|\\\\\\\\.)*\\\""

  private lazy val PATTERN = Pattern.compile(
    "(?<KEYWORD>" + KEYWORD_PATTERN + ")" +
    "|(?<STRING>" + STRING_PATTERN + ")" +
    "|(?<COMMENT>" + COMMENT_PATTERN + ")"
  )
}