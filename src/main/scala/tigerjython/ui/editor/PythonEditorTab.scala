/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.editor

import org.fxmisc.richtext._

/**
 * This is a specialisation of the more general `EditorTab` to use the Python-editor.
 *
 * @author Tobias Kohn
 */
class PythonEditorTab extends EditorTab {

  {
    caption.setValue("untitled %d".format(PythonEditorTab.nextNameIndex))
  }

  protected def createEditorNode: CodeArea = {
    val result = new PythonEditor()
    result.onAutoSave = autoSave
    result
  }
}
object PythonEditorTab {

  private var _nameCounter: Int = 0

  private def nextNameIndex: Int = {
    val result = _nameCounter + 1
    _nameCounter = result
    result
  }

  def apply(): PythonEditorTab = new PythonEditorTab()
}
