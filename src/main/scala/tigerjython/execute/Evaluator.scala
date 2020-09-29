/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

/**
 * The evaluator represents an interactive console or notebook, where the application is running continuously in the
 * background and reacting to user input.
 *
 * @author Tobias Kohn
 */
trait Evaluator {

  /**
   * Sends an expression to evaluate to the process.
   *
   * Because this happens asynchronously, the method does not return the result directly, but you need to provide an
   * object to which the results can then be sent.
   */
  def eval(expression: String, onResult: EvalResult): Unit

  def shutdown(): Unit
}
