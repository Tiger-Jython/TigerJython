/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.editor

import javafx.scene.control.{TextField, ToolBar}

/**
 * A simple bar that lets you enter something to be found in the document.
 */
class FindBar(val editorTab: EditorTab) extends ToolBar {

  lazy val findText: TextField = new TextField()

  {
    getItems.add(findText)
    setPrefHeight(48)
    setMinHeight(32)
  }
}
