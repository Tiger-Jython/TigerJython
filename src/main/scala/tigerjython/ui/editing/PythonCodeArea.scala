/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.editing

import java.time.Duration
import java.util.Optional

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.scene.input.{KeyCode, KeyEvent}
import org.fxmisc.richtext._
import org.fxmisc.richtext.model.{PlainTextChange, StyleSpans}
import tigerjython.core.Preferences
import tigerjython.plugins.EventManager
import tigerjython.ui.Utils.onFX

import tigerjython.syntaxsupport.SyntaxDocument

/**
 * The Python CodeArea inherits from _RichTextFX_'s `CodeArea` and customises it for highlighting Python code.
 *
 * @author Tobias Kohn
 */
class PythonCodeArea extends CodeArea {

  protected val undoQueue = new TigerJythonChangeQueue[PlainTextChange]()

  val syntaxDocument: SyntaxDocument = new SyntaxDocument()

  {
    setStyle("-fx-font-family: \"%s\";".format(Preferences.fontFamily.get))
    setUndoManager(UndoFactory.createUndoManager(this, undoQueue))

    this.multiPlainChanges()
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
  }

  addEventFilter(KeyEvent.KEY_TYPED, (key: KeyEvent) => {
    val char = key.getCharacter
    if (char != null && char != KeyEvent.CHAR_UNDEFINED) {
      val pos = getCaretPosition
      syntaxDocument.insert(pos, char)
      EventManager.fireOnKeyPressed(pos, char)
    }
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
          val pos = getCaretPosition
          onFX(() => { insertText(pos, text) })
          syntaxDocument.insert(pos, text)
          key.consume()
        }
        EventManager.fireOnKeyPressed(getCaretPosition, KeyCode.ENTER.toString)
      case KeyCode.TAB =>
        val tabWidth = Preferences.tabWidth.get
        val x = tabWidth - (getCaretColumn % tabWidth)
        val pos = getCaretPosition
        onFX(() => { insertText(pos, " " * x) })
        syntaxDocument.insert(pos, " " * x)
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
          syntaxDocument.delete(pos - back_width, back_width)
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

  private def computeHighlightingAsync(): Task[StyleSpans[java.util.Collection[String]]] =
    SyntaxHighlighter.computeHighlightingAsync(this.getText(), this.syntaxDocument)

  //    SyntaxHighlighter.computeHighlightingAsync(this.getText())

  private def applyHighlighting(highlighting: StyleSpans[java.util.Collection[String]]): Unit =
    if (highlighting != null)
      this.setStyleSpans(0, highlighting)

  def getUndoBuffer: Array[PlainTextChange] = {
    val result = new Array[PlainTextChange](undoQueue.getHistoryLength)
    for (i <- result.indices)
      result(i) = undoQueue.getHistoryItem(i)
    result
  }

  def setInitialText(text: String): Unit =
    Platform.runLater(() => {
      val txt: String =
        if (text != "" && text.last != '\n')
          text + "\n"
        else
          text
      appendText(txt)
      syntaxDocument.setText(txt)
      getUndoManager.forgetHistory()
      getUndoManager.mark()
      selectRange(0, 0)
    })

  def setInitialText(text: String, undoChanges: Array[PlainTextChange]): Unit =
    Platform.runLater(() => {
      val txt: String =
        if (text != "" && text.last != '\n')
          text + "\n"
        else
          text
      appendText(txt)
      syntaxDocument.setText(txt)
      getUndoManager.forgetHistory()
      undoQueue.push(undoChanges: _*)
      undoQueue.markPositionAsBase()
      getUndoManager.mark()
      selectRange(0, 0)
    })
}
