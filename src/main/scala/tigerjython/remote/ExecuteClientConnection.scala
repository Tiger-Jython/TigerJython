/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.remote

import java.io.{InputStream, ObjectInputStream, ObjectOutputStream, OutputStream}
import java.lang.management.ManagementFactory
import java.net.Socket
import java.util.{Timer, TimerTask}
import java.util.concurrent.ArrayBlockingQueue

import org.python.util.PythonInterpreter
import tigerjython.core.Configuration

/**
 * This is the part of the communication that lives inside the client and connects to the main TigerJython server.
 *
 * @author Tobias Kohn
 */
object ExecuteClientConnection extends Communicator {

  val HOST_NAME: String = "localhost"

  private var _id: Int = 0
  private var _socket: Socket = _
  private var _inputStream: InputStream = _
  private var _outputStream: OutputStream = _

  private lazy val memoryBean = ManagementFactory.getMemoryMXBean
  private lazy val threadBean = ManagementFactory.getThreadMXBean

  private val messageQueue = new ArrayBlockingQueue[Message](16)

  private val timer = new Timer()
  private object SystemInfoReporter extends TimerTask {
    override def run(): Unit =
      sendSystemStatus()
  }

  def socket: Socket = _socket
  def inputStream: InputStream = _inputStream
  def outputStream: OutputStream = _outputStream

  def id: Int = _id

  override protected def handleMessage(message: Message): Unit =
    message match {
      case QuitMessage() =>
        sendMessage(QuitMessage())
        quit()
      case _: EvalMessage | _: ExecFileMessage | _: ExecMessage =>
        messageQueue.put(message)
      case _ =>
        super.handleMessage(message)
    }

  def initialize(port: Int, id: Int): Unit = {
    _socket = new Socket(HOST_NAME, port)
    _id = id
    _inputStream = socket.getInputStream
    _outputStream = socket.getOutputStream
    thread.start()
    sendMessage(IDMessage(id))
    sendMessage(SystemInfoMessage(Configuration.getJavaVersion, Configuration.availableProcessors))
  }

  def processMessage(interpreter: PythonInterpreter): Unit =
    if (!messageQueue.isEmpty)
      messageQueue.poll() match {
        case EvalMessage(script, tag) =>
          try {
            timer.schedule(SystemInfoReporter, 0, 100)
            val result = interpreter.eval(script)
            sendMessage(ResultMessage(result.toString, tag))
          } catch {
            case e: Exception =>
              sendMessage(ErrorResultMessage(e.toString, tag))
          } finally {
            timer.cancel()
          }
        case ExecFileMessage(filename, tag) =>
          try {
            timer.schedule(SystemInfoReporter, 0, 100)
            interpreter.execfile(filename)
            sendMessage(ResultMessage(null, tag))
          } catch {
            case e: Exception =>
              sendMessage(ErrorResultMessage(e.toString, tag))
          } finally {
            timer.cancel()
          }
        case ExecMessage(script, tag) =>
          try {
            timer.schedule(SystemInfoReporter, 0, 100)
            interpreter.exec(script)
            sendMessage(ResultMessage(null, tag))
          } catch {
            case e: Exception =>
              sendMessage(ErrorResultMessage(e.toString, tag))
          } finally {
            timer.cancel()
          }
      }

  def quit(): Unit = {
    _socket.close()
    System.exit(0)
  }

  def sendSystemStatus(): Unit = {
    val memInfo = memoryBean.getHeapMemoryUsage
    val threadInfo = threadBean.getThreadCount
    sendMessage(SystemStatusMessage(System.currentTimeMillis, threadInfo, memInfo.getUsed, memInfo.getMax))
  }
}
