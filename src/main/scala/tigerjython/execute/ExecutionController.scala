/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

import java.io.File

/**
 * @author Tobias Kohn
 */
trait ExecutionController {

  def appendToErrorOutput(text: String): Unit

  def appendToLog(text: String): Unit

  def appendToOutput(text: String): Unit

  def clearOutput(): Unit

  def getExecutableFile: File

  def getExecutableFileAsString: String

  /**
   * Returns an iterator providing the names and source code of all the modules required to execute the program on the
   * given target platform/device.
   *
   * This is primarily meant to run an entire 'project' containing more than one module on an external device such as
   * the Micro:bit.
   */
  def getRequiredModules(target: String, execLanguage: ExecLanguage.Value): Iterable[(String, String)] = Iterable.empty

  def getText: String

  def handleError(errorText: String): Unit =
    appendToErrorOutput(errorText)

  def notifyExecutionFinished(executionTime: Long, terminated: Boolean = false): Unit =
    if (terminated) {
      appendToLog("process terminated after %d ms".format(executionTime))
    } else {
      appendToLog("finished after %d ms".format(executionTime))
      appendToOutput("--- finished in %d ms ---".format(executionTime))
    }

  def notifyExecutionStarted(): Unit = {}

  def updateRunStatus(executor: Executor, running: Boolean): Unit
}
