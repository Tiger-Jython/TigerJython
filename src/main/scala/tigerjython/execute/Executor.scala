/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

/**
 * The basic interface for anything that can execute a program: it must provide the means to run/start a program,
 * as well as to stop it.
 *
 * @author Tobias Kohn
 */
trait Executor {

  def controller: ExecutionController

  def run(): Unit

  def shutdown(): Unit

  def stop(): Unit

  def writeToInput(ch: Char): Unit

  def writeToInput(s: String): Unit
}
