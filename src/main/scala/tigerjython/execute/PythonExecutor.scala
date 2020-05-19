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
 * This is the interface to create an executor that attaches to an `EditorTab` and can run a Python program.
 *
 * @author Tobias Kohn
 */
abstract class PythonExecutor(val editorTab: EditorTab) extends Executor {

}
object PythonExecutor {

  /**
   * Creates a new Python executor, based on the currently selected Python interpreter.
   *
   * @param editorTab   The editor-tab that contains the code to be run.
   * @return            Either a `PythonExecutor`-instance to run the code, or `null`.
   */
  def apply(editorTab: EditorTab): PythonExecutor =
    if (PythonInstallations.useInternal) {
      null
    } else {
      val path = PythonInstallations.getSelectedPath
      if (path != null) {
        val process =
          if (path.toString.endsWith(".jar"))
            new PythonJavaProcess(path)
          else
            new PythonOSProcess(path)
        val result = new PythonOSExecutor(process, editorTab)
        process.parent = result
        result
      } else
        null
    }
}