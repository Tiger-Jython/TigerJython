/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.editor

import javafx.scene.control.TextArea
import javafx.scene.input.{KeyCode, KeyEvent}
import tigerjython.ui.ZoomMixin

/**
 * The default output pane uses a simple `TextArea` to display its text.
 *
 * @author Tobias Kohn
 */
class DefaultOutputPane(val editorTab: EditorTab, val captionID: String) extends OutputPane {

  import tigerjython.ui.Utils._

  private var _onKeyPress: Char=>Unit = _

  protected val textArea: TextArea = createTextArea

  { // set up the scene graph/user interface
    setClosable(false)
    setContent(textArea)
    setUICaption(captionID, textProperty())
    setUITooltip(captionID, tooltipProperty())

    textArea.addEventHandler(KeyEvent.KEY_PRESSED, (key: KeyEvent) => {
      if (key.getCode == KeyCode.ENTER && _onKeyPress != null) {
        _onKeyPress('\n')
        textArea.appendText("\n")
        key.consume()
      }
    })

    textArea.addEventHandler(KeyEvent.KEY_TYPED, (key: KeyEvent) => {
      if (_onKeyPress != null) {
        val s = key.getCharacter
        _onKeyPress(s(0))
        textArea.appendText(s)
        key.consume()
      }
    })
  }

  def append(s: String): Unit = onFX(() => {
    textArea.appendText(s)
  })

  def clear(): Unit = onFX(() => {
    textArea.clear()
  })

  protected def createTextArea: TextArea = {
    val result = new TextArea() with ZoomMixin
    result.setEditable(false)
    result.setWrapText(false)
    result.getStyleClass.add(captionID + "-pane")
    result
  }

  def getContentText: String =
    textArea.getText()

  def isEmpty: Boolean = {
    val text = textArea.getText
    text == null || text.length == 0
  }

  /**
   * Set the `onKeyPress` handler to enable keyboard input.
   */
  def onKeyPress: Char=>Unit = _onKeyPress
  def onKeyPress_=(keyPress: Char=>Unit): Unit =
    _onKeyPress = keyPress
}
