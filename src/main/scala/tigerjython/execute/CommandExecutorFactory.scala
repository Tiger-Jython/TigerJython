/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

import java.nio.file.Path

/**
 * @author Tobias Kohn
 */
class CommandExecutorFactory(val name: String,
                             val cmd: Path) extends ExecutorFactory {

  def createEvaluator(controller: Object, onReady: Evaluator=>Unit): Unit = {
    val evaluator = new ProcessEvaluator(new InteractiveOSProcess(getCommand))
    onReady(evaluator)
  }

  def createExecutor(controller: ExecutionController, onReady: Executor=>Unit): Unit = {
    val executor = new CommandExecutor(new InterpreterProcess(getCommand), controller)
    onReady(executor)
  }

  protected def getCommand: String = cmd.toAbsolutePath.toString
}
