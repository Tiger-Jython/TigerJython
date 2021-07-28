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
class DelimiterToken(val char: Char) extends Token(if (char == ',') TokenType.COMMA else TokenType.DELIMITER, 1) {

  override def equals(obj: Any): Boolean =
    obj match {
      case delimiterToken: DelimiterToken =>
        char == delimiterToken.char
      case TokenType.DELIMITER =>
        true
      case ch: Char =>
        char == ch
      case _ =>
        super.equals(obj)
    }

  def text: String = char.toString

  override def toString: String = f"delimiter($char)"
}

object DelimiterToken {

  def unapply(token: Token): Option[Char] =
    token match {
      case delimiterToken: DelimiterToken =>
        Some(delimiterToken.char)
      case _: ColonToken =>
        Some(':')
      case _ =>
        None
    }
}
