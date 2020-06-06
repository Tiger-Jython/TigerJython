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
trait EvalResult {

  def setError(errorMsg: String): Unit

  def setResult(result: String): Unit
}
