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
class PythonOSEvaluator(val process: PythonOSIProcess) extends PythonEvaluator {

  def eval(expression: String, onResult: EvalResult): Unit =
    process.request(expression, onResult)
}
