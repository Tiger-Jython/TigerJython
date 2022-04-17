/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

import tigerjython.remote.{ExecuteServer, QuitMessage}

/**
 * This executor uses a simple "OS terminal command" to execute the code.  That is, it does not support any direct
 * interaction other than through standard input/output.
 *
 * @author Tobias Kohn
 */
class TigerJythonExecutor(val id: Int, override val controller: ExecutionController) extends Executor with Evaluator {

  protected val process: InterpreterProcess = TigerJythonProcess(id)

  {
    process.parent = this
    process.isClientProcess = true
    controller.appendToLog("Starting process:")
    controller.appendToLog(process.getCommandText)
    process.onCompleted = _ => {
      TigerJythonExecutorFactory.removeExecutor(this)
    }
    TigerJythonExecutorFactory.addExecutor(this)
    if (!process.isRunning)
      process.exec("-client", ExecuteServer.getPort.toString, id.toString)
  }

  def run(): Unit =
    ExecuteServer.waitForProxy(id, proxy => {
      controller.appendToLog("Connected to client %d".format(id))
      val _file = controller.getExecutableFile
      val filename = _file.getAbsolutePath
      controller.appendToLog("Running file: %s".format(filename))
      controller.clearOutput()
      controller.notifyExecutionStarted()
      controller.updateRunStatus(this, running = true)
      val startTime = System.currentTimeMillis()
      proxy.executeFile(filename, (msg, isError) => {
        val runTime = System.currentTimeMillis() - startTime
        proxy.sendMessage(QuitMessage())
        Thread.sleep(250)    // Leave some time for the output to appear in the editor window
        if (isError)
          controller.handleError(msg)
        controller.notifyExecutionFinished(runTime)
      })
    })

  def shutdown(): Unit = {
    ExecuteServer(id) match {
      case Some(proxy) =>
        proxy.quit()
        Thread.sleep(100)   // Let's give the process a chance to end 'normally'
      case _ =>
    }
    process.abort()
  }

  def stop(): Unit =
    ExecuteServer(id) match {
      case Some(proxy) =>
        process.abort()
      case None =>
        process.abort()
    }

  def eval(expression: String, onResult: EvalResult): Unit =
    ExecuteServer.waitForProxy(id, proxy => {
      proxy.evaluate(expression, (result, isError) => {
        if (isError)
          onResult.setError(result)
        else
          onResult.setResult(result)

      })
    })

  def writeToInput(ch: Char): Unit =
    process.writeToInput(ch)

  def writeToInput(s: String): Unit =
    process.writeToInput(s)
}
