/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.struct

/**
 * @author Tobias Kohn
 */
object NameType extends Enumeration {

  final val LOCAL = Value
  final val GLOBAL = Value
  final val PARAMETER = Value
  final val NONLOCAL = Value
  final val FREE = Value

}
