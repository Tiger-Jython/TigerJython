/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

import tigerjython.ui.editor.EditorTab

/**
 * An implementation of the executor interface that runs the Python script externally.
 *
 * @author Tobias Kohn
 */
class PythonOSExecutor(val process: PythonOSProcess, editor_tab: EditorTab) extends PythonExecutor(editor_tab) {

  def run(): Unit =
    process.exec(editorTab.getExecutableFile.getAbsolutePath)

  def stop(): Unit =
    process.abort()

  def writeToInput(ch: Char): Unit =
    process.writeToInput(ch)

  def writeToInput(s: String): Unit =
    process.writeToInput(s)
}
