/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

import tigerjython.core.Preferences

/**
 * This executor uses a simple "OS terminal command" to execute the code.  That is, it does not support any direct
 * interaction other than through standard input/output.
 *
 * @author Tobias Kohn
 */
object TigerJythonExecutorFactory extends ExecutorFactory {

  val name: String = "TigerJython"

  private var ids: Int = 0

  protected def newInstance(controller: ExecutionController): TigerJythonExecutor = {
    ids += 1
    new TigerJythonExecutor(ids, controller)
  }

  def createEvaluator(controller: ExecutionController, onReady: Evaluator=>Unit): Unit = {
    onReady(newInstance(controller))
  }

  def createExecutor(controller: ExecutionController, onReady: Executor=>Unit): Unit = {
    onReady(newInstance(controller))
  }

  def getExecLanguage: ExecLanguage.Value = ExecLanguage.PYTHON_2
}
