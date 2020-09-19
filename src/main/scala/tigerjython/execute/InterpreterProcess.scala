/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

import java.nio.file.Path
import java.util.{Timer, TimerTask}

import tigerjython.utils.OSProcess

/**
 * In addition to other OS processes, the `InterpreterProcess` will also update the controller by routing any output to
 * its output pane, as well as indicating when the process has run its course.
 *
 * @author Tobias Kohn
 */
class InterpreterProcess(cmd: String) extends OSProcess(cmd) {

  var parent: Executor = _

  var isClientProcess: Boolean = false

  def this(cmd: Path) =
    this(cmd.toAbsolutePath.toString)

  protected def controller: ExecutionController = parent.controller

  override protected def getBaseArgs: Array[String] =
    if (isJavaProcess)
      Array("-S")
    else
      Array()

  protected object UpdateTask extends TimerTask {

    override def run(): Unit =
      if (controller != null) {
        if (stdError.nonEmpty) {
          val err = stdError.readBuffer()
          controller.appendToErrorOutput(err)
        }
        val out = stdOutput.readBuffer()
        if (out != null && out != "")
          controller.appendToOutput(out)
      }
  }

  protected var timer: Timer = _

  override def started(): Unit = {
    if (controller != null) {
      if (!isClientProcess) {
        controller.notifyExecutionStarted()
        controller.updateRunStatus(parent, running = true)
      }
      timer = new Timer(true)
      timer.scheduleAtFixedRate(UpdateTask, 100, 100)
    }
  }

  override def completed(result: Int): Unit = {
    if (timer != null) {
      timer.cancel()
      timer = null
    }
    if (controller != null) {
      UpdateTask.run()  // read the last bits of the output and error streams
      controller.updateRunStatus(parent, running = false)
      controller.notifyExecutionFinished(getExecTime, terminated=isClientProcess)
    }
  }
}
