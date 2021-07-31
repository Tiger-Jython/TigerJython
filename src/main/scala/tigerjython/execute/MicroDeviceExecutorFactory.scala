/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

/**
 * @author Tobias Kohn
 */
class MicroDeviceExecutorFactory(val name: String,
                                 val execLanguage: ExecLanguage.Value) extends ExecutorFactory {

  def createEvaluator(controller: ExecutionController, onReady: Evaluator=>Unit): Unit =
    throw new RuntimeException("no evaluator available!")

  def createExecutor(controller: ExecutionController, onReady: Executor=>Unit): Unit = {
    val executor = new MicroDeviceExecutor(controller, name)
    onReady(executor)
  }

  def getExecLanguage: ExecLanguage.Value = execLanguage
}
