/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.utils

import java.io._
import java.nio.file.Path
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

/**
 * This class encapsulates an OS-process.  It provides an interface to read the processes output and error stream, as
 * well as send text to its input stream.
 *
 * @author Tobias Kohn
 */
class OSProcess(val cmd: String) {

  def this(file: File) =
    this(file.getAbsolutePath)

  def this(path: Path) =
    this(path.toAbsolutePath.toString)

  private val _execResult = new AtomicInteger()
  private var _execTime: Long = 0
  private val _isRunning = new AtomicBoolean(false)

  /**
   * When running a child process, we must make sure to frequently read its output buffers to avoid a buffer overflow,
   * but also to get the desired information.  We therefore instantiate a `ThreadReader` in a separate thread, where
   * it continuously reads the respective output-stream and adds it to an internal buffer.
   *
   * @param inputStream   The stream from which to read.
   */
  protected class ThreadReader(val inputStream: InputStream) extends Runnable {

    private val buffer = new ArrayBlockingQueue[Char](1024)

    def addToBuffer(ch: Char): Unit =
      buffer.put(ch)

    def isEmpty: Boolean = buffer.isEmpty

    def nonEmpty: Boolean = !isEmpty

    def readBuffer(): String = {
      val length = buffer.size()
      if (length > 0) {
        val _buffer = new Array[Char](length)
        for (i <- 0 until length)
          _buffer(i) = buffer.take()
        new String(_buffer)
      } else
        ""
    }

    def run(): Unit = {
      val reader = new InputStreamReader(inputStream)
      var char = reader.read()
      while (char >= 0) {
        addToBuffer(char.toChar)
        char = reader.read()
      }
      reader.close()
    }
  }

  /**
   * We monitor the state of the process in a separate thread to get notified when the process has stopped and get its
   * result (error code).
   */
  protected object ProcessObserver extends Runnable {

    def run(): Unit = {
      while (process == null)
        Thread.sleep(10)
      val result = process.waitFor()
      _execTime = System.currentTimeMillis() - _execTime
      _completed(result)
    }
  }

  protected var process: Process = _
  protected val runtime: Runtime = Runtime.getRuntime
  protected var stdError: ThreadReader = _
  protected var stdInput: OutputStreamWriter = _
  protected var stdOutput: ThreadReader = _

  /**
   * Set a custom function to be executed when the process has stopped running.
   */
  var onCompleted: Int=>Unit = _

  def abort(): Unit =
    if (process != null) synchronized {
      process.destroy()
      //_execTime = System.currentTimeMillis() - _execTime
      //_completed(-1)
    }

  private def _completed(result: Int): Unit = {
    _execResult.set(result)
    if (stdInput != null)
      try {
        stdInput.close()
      } catch {
        case _: java.io.IOException =>
      }
    stdInput = null
    _isRunning.set(false)
    completed(result)
  }

  protected def completed(result: Int): Unit =
    if (onCompleted != null)
      onCompleted(result)

  protected def createCommands(args: Seq[String]): Array[String] =
    Array(cmd) :++ args

  /**
   * Run the process with the given arguments.
   *
   * @param args   A sequence of (string) arguments to be passed on to the process.
   * @return       `true` if the process was started, `false` if an error occurred.
   */
  def exec(args: String*): Boolean =
    try {
      if (_isRunning.get())
        return false
      val commands = createCommands(args)
      val observer = new Thread(ProcessObserver)
      _execTime = System.currentTimeMillis()
      process = runtime.exec(commands)
      _execTime = (_execTime + System.currentTimeMillis()) / 2
      observer.start()
      stdOutput = new ThreadReader(process.getInputStream)
      stdError = new ThreadReader(process.getErrorStream)
      stdInput = new OutputStreamWriter(process.getOutputStream)
      new Thread(stdOutput).start()
      new Thread(stdError).start()
      _isRunning.set(true)
      started()
      true
    } catch {
      case _: IOException =>
        _execResult.set(-1)
        false
      case _: SecurityException =>
        _execResult.set(-1)
        false
    }

  /**
   * Returns the execution time of the process in milliseconds.
   *
   * This is only approximate because of the overhead of starting the external process and being notified about it
   * having finished.
   */
  def getExecTime: Long = _execTime

  /**
   * The result of a process typically indicates any errors, where a result of `0` means no errors.
   */
  def getResult: Int = _execResult.get()

  /**
   * Returns `true` as long as the process is running.
   */
  def isRunning: Boolean = _isRunning.get()

  /**
   * Read from the processes' error output stream.
   *
   * Note that reading from the stream removes the respective bytes/chars from the stream.
   */
  def readFromError(): String =
    if (stdError != null)
      stdError.readBuffer()
    else
      null

  /**
   * Read from the processes' standard output stream.
   *
   * Note that reading from the stream removes the respective bytes/chars from the stream.
   */
  def readFromOutput(): String =
    if (stdOutput != null)
      stdOutput.readBuffer()
    else
      null

  protected def started(): Unit = {}

  /**
   * Wait until the process has finished and then return the output.
   *
   * This is primarily intended for running small helper script that return quickly and which are used to obtain some
   * (textual) information.
   */
  def waitForOutput(): String =
    try {
      while (_isRunning.get())
        Thread.sleep(50)
      if (stdOutput != null)
        stdOutput.readBuffer()
      else
        null
    } catch {
      case _: InterruptedException =>
        null
    }

  /**
   * Write a specific character to the processes' input stream.
   */
  def writeToInput(ch: Char): Unit =
    if (stdInput != null) {
      stdInput.write(ch.toInt)
      if (ch == '\n')
        stdInput.flush()
    }

  /**
   * Write a specific string to the processes' input stream.
   */
  def writeToInput(s: String): Unit =
    if (s != null && s != "" && stdInput != null) {
      if (s == "\n") {
        stdInput.write(s, 0, s.length)
        stdInput.flush()
      } else
        stdInput.write(s, 0, s.length)
    }
}
