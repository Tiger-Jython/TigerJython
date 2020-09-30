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
object ExecLanguage extends Enumeration {

  final val PYTHON_2 = Value

  final val PYTHON_3 = Value

  def python(version: Int): Value =
    version match {
      case 2 => PYTHON_2
      case _ => PYTHON_3
    }
}
