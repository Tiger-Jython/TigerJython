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
class NewlineToken(l: Int) extends Token(TokenType.NEWLINE, l) {

  override def equals(obj: Any): Boolean =
    obj match {
      case _: NewlineToken =>
        true
      case TokenType.NEWLINE =>
        true
      case ch: Char =>
        ch == '\n'
      case _ =>
        super.equals(obj)
    }

  override def toString: String = "<NEWLINE>"
}
