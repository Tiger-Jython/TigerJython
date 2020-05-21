/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.plugins

import javafx.application.Platform

/**
 * This is a helper class for running code on the FX thread and get some response from it (even if it is just to say
 * "done" or "task completed").
 *
 * @author Tobias Kohn
 */
class FXExecutor[T](val body: ()=>T) extends Runnable {

  //  private val finished = new java.util.concurrent.atomic.AtomicBoolean(false)
  private var result: T = _

  def run(): Unit = synchronized {
    result = body()
    notifyAll()
  }

  def get(): T = synchronized {
    wait()
    result
  }
}
object FXExecutor {

  def apply[T](body: ()=>T): T =
    if (!Platform.isFxApplicationThread) {
      val executor = new FXExecutor[T](body)
      Platform.runLater(executor)
      executor.get()
    } else
      body()
}