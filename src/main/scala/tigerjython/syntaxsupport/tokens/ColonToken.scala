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
class ColonToken extends Token(TokenType.COLON, 1) {

  private var _isCompoundColon: Boolean = false

  override def equals(obj: Any): Boolean =
    obj match {
      case _: ColonToken =>
        true
      case TokenType.COLON =>
        true
      case ch: Char =>
        ch == ':'
      case _ =>
        super.equals(obj)
    }

  def isCompoundColon: Boolean = _isCompoundColon

  def text: String = ":"

  override def toString: String = "colon"
}
