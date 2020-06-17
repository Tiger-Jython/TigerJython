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
import javafx.css.PseudoClass
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.BorderPane
import tigerjython.execute.{EvalResult, Evaluator}

/**
 * @author Tobias Kohn
 */
class NotebookCell(val notebook: NotebookTab) extends BorderPane with EvalResult {

  import tigerjython.ui.Utils.onFX

  protected val editor: NotebookEditor = new NotebookEditor(this)
  private var _result: Node = _

  {
    setCenter(editor)
    getStyleClass.add("cell")
    if (notebook != null)
      prefWidthProperty().bind(notebook.widthProperty().subtract(20))

    editor.addEventFilter(KeyEvent.KEY_PRESSED, (event: KeyEvent) => {
      if (event.getCode == KeyCode.ENTER && (event.isShiftDown || event.isControlDown)) {
        notebook.selectCell(this)
        notebook.evaluateCell()
        event.consume()
      }
    })

    editor.focusedProperty().addListener(new ChangeListener[lang.Boolean] {
      override def changed(observableValue: ObservableValue[_ <: lang.Boolean], t: lang.Boolean, focused: lang.Boolean): Unit = {
        if (focused)
          notebook.selectCell(NotebookCell.this)
      }
    })
  }

  def recomputeHeight(): Unit =
    editor.recomputeHeight()

  def setTextSize(size: Double): Unit =
    editor.setTextSize(size)

  def setActive(active: Boolean): Unit = {
    pseudoClassStateChanged(NotebookCell.activePseudoClass, active)
    if (active)
      Platform.runLater(() => {
        editor.requestFocus()
      })
  }

  protected def resultNode: Node = _result
  protected def resultNode_=(r: Node): Unit = {
    _result = r
    if (_result != null)
      setBottom(_result)
    else
      setBottom(null)
  }

  def setError(errorMsg: String): Unit =
    onFX(() => {
      val result = new Label(errorMsg)
      result.getStyleClass.add("error")
      resultNode = result
    })

  def setResult(text: String): Unit =
    onFX(() => {
      if (text != "") {
        val result = new Label(text)
        result.getStyleClass.add("output")
        resultNode = result
      } else
        resultNode = null
      notebook.selectNextCell()
    })

  def evaluate(evaluator: Evaluator): Unit = {
    evaluator.eval(editor.getText, this)
  }
}
object NotebookCell {

  private val activePseudoClass = PseudoClass.getPseudoClass("active")

  def apply(notebook: NotebookTab): NotebookCell =
    new NotebookCell(notebook)
}