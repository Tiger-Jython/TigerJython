/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

import tigerjython.remote.{ExecFileMessage, ExecuteServer}

/**
 * This executor uses a simple "OS terminal command" to execute the code.  That is, it does not support any direct
 * interaction other than through standard input/output.
 *
 * @author Tobias Kohn
 */
class TigerJythonExecutor(val id: Int, override val controller: ExecutionController) extends Executor {

  protected val process: InterpreterProcess = new InterpreterProcess(PythonInstallations.getInternalJythonPath)

  {
    process.parent = this
    controller.appendToLog("Starting process:")
    controller.appendToLog(process.getCommandText)
    process.exec("-client", ExecuteServer.getPort.toString, id.toString)
  }

  def run(): Unit =
    ExecuteServer.waitForProxy(id, proxy => {
      controller.appendToLog("Connexted to client %d".format(id))
      val filename = controller.getExecutableFile.getAbsolutePath
      controller.appendToLog("Running file: %s".format(filename))
      proxy.sendMessage(ExecFileMessage(filename, 0))
    })

  def stop(): Unit =
    ExecuteServer(id) match {
      case Some(proxy) =>
        process.abort()
      case None =>
        process.abort()
    }

  def writeToInput(ch: Char): Unit =
    process.writeToInput(ch)

  def writeToInput(s: String): Unit =
    process.writeToInput(s)
}
