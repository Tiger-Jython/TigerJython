/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

import tigerjython.core.Preferences
import tigerjython.remote.ExecuteServer

/**
 * @author Tobias Kohn
 */
object TigerJythonProcess {

  private var cache: InterpreterProcess = _

  private var cacheId: Int = 0x10000000

  private def startCache: InterpreterProcess = {
    cacheId += 1
    val process = new InterpreterProcess(PythonInstallations.getInternalJythonPath)
    process.exec("-client", ExecuteServer.getPort.toString, cacheId.toString)
    process
  }

  def apply(id: Int): InterpreterProcess = synchronized {
    if (cache != null) {
      val result = cache
      ExecuteServer.updateClientId(cacheId, id)
      cache = null
      result
    } else
      new InterpreterProcess(PythonInstallations.getInternalJythonPath)
  }

  def initialize(): Unit = {
    preStart()
  }

  def preStart(): Unit = synchronized {
    if (Preferences.preStartExecutor.get && cache == null) {
      cache = startCache
    }
  }

  def shutdown(): Unit =
    if (cache != null)
      cache.abort()
}
