/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

/**
 * @author Tobias Kohn
 */
trait ExecutorFactory {

  def canEvaluate: Boolean = true

  def canExecute: Boolean = true

  def createEvaluator(controller: Object, onReady: Evaluator=>Unit): Unit

  def createExecutor(controller: ExecutionController, onReady: Executor=>Unit): Unit

  def name: String
}
