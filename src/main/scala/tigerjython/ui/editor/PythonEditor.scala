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

    addEventFilter(KeyEvent.KEY_PRESSED, (key: KeyEvent) => {
      if (key.getCode == KeyCode.ENTER) {
        val currentLine = getText(getCurrentParagraph)
        var indentation = currentLine.takeWhile(_ == ' ')
        if (currentLine.endsWith(":"))
          indentation += " " * 4
        if (indentation != "") {
          val text = "\n" + indentation
          onFX(() => { insertText(getCaretPosition, text) })
          key.consume()
        }
        // getStyleAtPosition(0)
        // getParagraphs.get(1).getStyledSegments
        // getTextStyleForInsertionAt
      }/* else
      if (key.getCode == KeyCode.Y) {
        /* println(getStyleAtPosition(getCaretPosition))
        getStyleAtPosition(getCaretPosition) */
        println(getParagraphs.get(getCurrentParagraph)
          .getSegments.get(getCaretColumn))
        setStyle()
      }*/
    })
    addEventFilter(KeyEvent.KEY_RELEASED, (key: KeyEvent) => {
      if (key.getCode == KeyCode.Y) {
        val pos = getCaretPosition
        setStyleClass(pos-1, pos, "active-var")
      }
    })

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
        else if (matcher.group("VAR") != null)
          "active-var"
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
    "def", "else", "from", "if", "import", "pass", "return", "while"
  )

  private val KEYWORD_PATTERN = "\\b(" + KEYWORDS.mkString("|") + ")\\b"

  private lazy val PATTERN = Pattern.compile(
    "(?<KEYWORD>" + KEYWORD_PATTERN + ")" +
      "|(?<VAR>\\b(x)\\b)"
  )
}