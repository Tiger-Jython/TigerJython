/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.editing

import java.util.concurrent.Executors
import javafx.concurrent.Task

/**
 * @author Tobias Kohn
 */
object BackgroundSaver {

  private val executor = Executors.newSingleThreadExecutor()

  def execute(command: Runnable): Unit =
    if (command != null)
      executor.execute(command: Runnable)

  def execute(command: ()=>Unit): Task[Unit] =
    if (command != null) {
      val task = new Task[Unit]() {
        override def call(): Unit = {
          command()
        }
      }
      executor.execute(task)
      task
    } else
      null
}
