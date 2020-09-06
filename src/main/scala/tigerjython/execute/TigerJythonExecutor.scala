/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

import tigerjython.remote.ExecuteServer

/**
 * This executor uses a simple "OS terminal command" to execute the code.  That is, it does not support any direct
 * interaction other than through standard input/output.
 *
 * @author Tobias Kohn
 */
class TigerJythonExecutor(val id: Int, val controller: ExecutionController) extends Executor {

  protected val process: InterpreterProcess = new InterpreterProcess(PythonInstallations.getInternalJythonPath)
  process.exec("-client", ExecuteServer.getPort.toString, id.toString)

  def run(): Unit =
    ExecuteServer(id) match {
      case Some(proxy) =>
//        process.exec(controller.getExecutableFile.getAbsolutePath)
      case _ =>
    }

  def stop(): Unit =
    process.abort()

  def writeToInput(ch: Char): Unit =
    process.writeToInput(ch)

  def writeToInput(s: String): Unit =
    process.writeToInput(s)
}
