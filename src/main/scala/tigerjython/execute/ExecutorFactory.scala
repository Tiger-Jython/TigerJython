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

  private val evaluators = collection.mutable.Set[Evaluator]()

  private val executors = collection.mutable.Set[Executor]()

  private[execute] def addEvaluator(evaluator: Evaluator): Unit =
    if (evaluator != null)
      evaluators += evaluator

  private[execute] def addExecutor(executor: Executor): Unit =
    if (executor != null)
      executors += executor

  private[execute] def removeEvaluator(evaluator: Evaluator): Unit =
    evaluators -= evaluator

  private[execute] def removeExecutor(executor: Executor): Unit =
    executors -= executor

  def canEvaluate: Boolean = true

  def canExecute: Boolean = true

  def createEvaluator(controller: ExecutionController, onReady: Evaluator=>Unit): Unit

  def createExecutor(controller: ExecutionController, onReady: Executor=>Unit): Unit

  def getExecLanguage: ExecLanguage.Value

  def name: String

  def shutdown(): Unit = {
    if (evaluators.nonEmpty)
      for (evaluator <- evaluators.clone())
        evaluator.shutdown()
    if (executors.nonEmpty)
      for (executor <- executors.clone())
        executor.shutdown()
  }
}
