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
 *
 * @author Tobias Kohn
 */
class InteractiveOSProcess(cmd: String) extends OSProcess(cmd) {

  def this(cmd: Path) =
    this(cmd.toAbsolutePath.toString)

  private val prompt: String = "\u0007\u0005"

  protected case class Request(expression: String, onResult: EvalResult)

  private val requests = collection.mutable.Queue[Request]()

  private var _currentRequest: Request = _

  def request(expression: String, onResult: EvalResult): Unit =
    if (expression != null && expression != "")
      synchronized {
        val expr =
          if (expression.endsWith("\n"))
            expression + "\n"
          else
            expression + "\n\n"
        requests.enqueue(Request(expr, onResult))
        if (!isRunning)
          exec("-i")
      }

  protected def closeRequest(): Unit =
    synchronized {
      _currentRequest = null
    }

  protected def getCurrentRequest: Option[Request] =
    Option(_currentRequest)

  protected def getNextRequest: Option[Request] =
    synchronized {
      if (_currentRequest == null && requests.nonEmpty) {
        val result = requests.dequeue()
        _currentRequest = result
        Some(result)
      } else
        None
    }

  protected object UpdateTask extends TimerTask {

    private val outputBuffer = new StringBuilder()

    override def run(): Unit = {
      if (_currentRequest != null) {
        outputBuffer ++= stdOutput.readBuffer()
        if (stdError.nonEmpty) {
          var text = stdError.readBuffer()
          while (text.startsWith(prompt))
            text = text.drop(prompt.length)
          while (text.endsWith(prompt))
            text = text.dropRight(prompt.length)
          if (text != "")
            _currentRequest.onResult.setError(text)
          else {
            while (outputBuffer.nonEmpty && outputBuffer.last == '\n')
              outputBuffer.deleteCharAt(outputBuffer.length-1)
            _currentRequest.onResult.setResult(outputBuffer.toString)
            outputBuffer.clear()
          }
          closeRequest()
        }
      } else
      getNextRequest match {
        case Some(Request(expr, _)) =>
          stdInput.write(expr)
          stdInput.flush()
        case _ =>
      }
    }
  }

  protected var timer: Timer = _

  override def started(): Unit = {
    var buffer = new StringBuilder()
    while (!buffer.toString.contains(">>>")) {
      Thread.sleep(10)
      buffer ++= stdError.readBuffer()
    }
    buffer.clear()
    stdInput.write("import sys as _sys\n")
    stdInput.flush()
    stdInput.write("_sys.ps1 = \"%s\"; _sys.ps2 = ''; del _sys\n".format(prompt))
    stdInput.flush()
    while (!buffer.toString.contains(prompt)) {
      Thread.sleep(10)
      buffer ++= stdError.readBuffer()
    }
    timer = new Timer(true)
    timer.scheduleAtFixedRate(UpdateTask, 100, 100)
  }

  override def completed(result: Int): Unit = {
    if (timer != null) {
      timer.cancel()
      timer = null
    }
    UpdateTask.run()  // read the last bits of the output and error streams
  }
}
