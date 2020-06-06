/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

import tigerjython.ui.notebook.NotebookTab

/**
 * @author Tobias Kohn
 */
abstract class PythonEvaluator extends Evaluator {

}
object PythonEvaluator {

  def apply(notebookTab: NotebookTab): PythonEvaluator =
    if (PythonInstallations.useInternal) {
      null
    } else {
      val path = PythonInstallations.getSelectedPath
      if (path != null) {
        val process =
          if (path.toString.endsWith(".jar"))
            null // new PythonJavaProcess(path)
          else
            new PythonOSIProcess(path)
        if (process != null)
          new PythonOSEvaluator(process)
        else
          null
      } else
        null
    }
}