/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.remote

import java.io.{InputStream, OutputStream}
import java.net.Socket

import tigerjython.core.Configuration

/**
 * An instance of this class represents a remote instance of a JVM running TigerJython with support for communication
 * over sockets.  Use it to send message/commands to the remote system as well as query the state of the system.
 *
 * @author Tobias Kohn
 */
class ExecuteClientProxy(val socket: Socket) extends Communicator {

  private var _id: Int = 0

  private var _cpuCount: Int = Configuration.availableProcessors
  private var _javaVersion: Int = Configuration.getJavaVersion

  private var _lastUpdate: Long = 0
  private var _threadCount: Int = 0
  private var _memoryUsed: Long = 0
  private var _memoryMax: Long = 0

  private val queryBuffer = collection.mutable.Map[Int, (String, Boolean)=>Unit]()
  private var _tag: Int = 0

  val inputStream: InputStream = socket.getInputStream
  val outputStream: OutputStream = socket.getOutputStream

  def availableProcessors: Int = _cpuCount
  def id: Int = _id
  def javaVersion: Int = _javaVersion

  def currentThreadCount: Int = _threadCount
  def currentMemoryInUse: Long = _memoryUsed
  def currentMemoryMax: Long = _memoryMax

  override protected def handleMessage(message: Message): Unit =
    message match {
      case IDMessage(id) =>
        this._id = id
        ExecuteServer.addClient(this)
      case QuitMessage() =>
        ExecuteServer.removeClient(this)
      case ErrorResultMessage(errorMsg, tag) =>
        queryBuffer.remove(tag) match {
          case Some(onResult) =>
            onResult(errorMsg, true)
          case None =>
        }
      case ResultMessage(result, tag) =>
        queryBuffer.remove(tag) match {
          case Some(onResult) =>
            onResult(result, false)
          case None =>
        }
      case SystemInfoMessage(javaVersion, cpuCount) =>
        _cpuCount = cpuCount
        _javaVersion = javaVersion
      case SystemStatusMessage(time, threadCount, memoryUsed, memoryMax) =>
        _lastUpdate = time
        _threadCount = threadCount
        _memoryUsed = memoryUsed
        _memoryMax = memoryMax
      case _ =>
        super.handleMessage(message)
    }

  private def nextTag: Int = {
    _tag += 1
    _tag
  }

  def evaluate(script: String, onResult: (String, Boolean)=>Unit): Unit = {
    val tag = nextTag
    queryBuffer(tag) = onResult
    sendMessage(EvalMessage(script, tag))
  }

  def executeFile(fileName: String, onResult: (String, Boolean)=>Unit): Unit = {
    val tag = nextTag
    queryBuffer(tag) = onResult
    sendMessage(ExecFileMessage(fileName, tag))
  }

  thread.start()
}
