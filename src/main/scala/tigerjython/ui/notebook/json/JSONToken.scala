/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.notebook.json

private[json] sealed abstract class JSONToken {
  val line: Int

  override def toString: String =
    getClass.getSimpleName + s"[line:$line]"
}

private[json] object JSONToken {

  case class BooleanValue(value: Boolean, line: Int) extends JSONToken
  case class Colon(line: Int) extends JSONToken
  case class Comma(line: Int) extends JSONToken
  case class LeftBrace(line: Int) extends JSONToken
  case class LeftBracket(line: Int) extends JSONToken
  case class IntegerValue(value: Int, line: Int) extends JSONToken
  case class Name(value: String, line: Int) extends JSONToken
  case class NumberValue(value: Double, line: Int) extends JSONToken
  case class RightBrace(line: Int) extends JSONToken
  case class RightBracket(line: Int) extends JSONToken
  case class StringValue(value: String, line: Int) extends JSONToken {
    override def toString: String = s"String[Line:$line, value:$value]"
  }
}
