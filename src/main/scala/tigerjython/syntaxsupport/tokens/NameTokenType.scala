/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.tokens

/**
 * @author Tobias Kohn
 */
object NameTokenType extends Enumeration {

  final val UNKNOWN = Value
  final val DEF_NAME = Value
  final val LOAD = Value
  final val CALL = Value
  final val SUBSCRIPT = Value
  final val ASSIGN_TARGET = Value
  final val FORMAL_ARG = Value
  final val ARG_KEYWORD = Value
  final val DEL_TARGET = Value
  final val GLOBALIZED = Value
  final val ATTRIBUTE = Value
  final val MODULE = Value
  final val STORE = Value
  final val LOCAL_STORE = Value
  final val LAMBDA_PARAM = Value
  final val LAMBDA_ARG = Value

}
