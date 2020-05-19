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

import tigerjython.ui.editor.EditorTab
import tigerjython.utils.OSProcess

/**
 * In addition to other OS processes, the `PythonOSProcess` will also update the editor-tab by routing any output to
 * its output pane, as well as indicating when the process has run its course.
 *
 * @author Tobias Kohn
 */
class PythonOSProcess(cmd: String) extends OSProcess(cmd) {

  var parent: PythonOSExecutor = _

  def this(cmd: Path) =
    this(cmd.toAbsolutePath.toString)

  protected def editorTab: EditorTab = parent.editorTab

  protected object UpdateTask extends TimerTask {

    override def run(): Unit =
      if (editorTab != null) {
        if (stdError.nonEmpty) {
          val err = stdError.readBuffer()
          editorTab.appendToErrorOutput(err)
        }
        val out = stdOutput.readBuffer()
        if (out != null && out != "")
          editorTab.appendToOutput(out)
      }
  }

  protected var timer: Timer = _

  override def started(): Unit = {
    if (editorTab != null) {
      editorTab.updateRunStatus(parent, running = true)
      timer = new Timer(true)
      timer.scheduleAtFixedRate(UpdateTask, 100, 100)
    }
  }

  override def completed(result: Int): Unit = {
    if (timer != null) {
      timer.cancel()
      timer = null
    }
    if (editorTab != null) {
      UpdateTask.run()  // read the last bits of the output and error streams
      editorTab.updateRunStatus(parent, running = false)
      editorTab.appendToOutput("--- finished in %d ms ---".format(getExecTime))
    }
  }
}
