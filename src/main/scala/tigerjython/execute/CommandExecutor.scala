/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

/**
 * This executor uses a simple "OS terminal command" to execute the code.  That is, it does not support any direct
 * interaction other than through standard input/output.
 *
 * @author Tobias Kohn
 */
class CommandExecutor(val process: InterpreterProcess,
                      val controller: ExecutionController) extends Executor {

  process.parent = this

  def run(): Unit = {
    val filename = controller.getExecutableFile.getAbsolutePath
    controller.appendToLog("Executing '%s'".format(filename))
    controller.appendToLog(process.getCommandText)
    controller.clearOutput()
    process.exec(filename)
  }

  override def shutdown(): Unit =
    process.abort()

  def stop(): Unit =
    process.abort()

  def writeToInput(ch: Char): Unit =
    process.writeToInput(ch)

  def writeToInput(s: String): Unit =
    process.writeToInput(s)
}
