/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.tokens

import tigerjython.syntaxsupport.{SyntaxDocument, TokenVisitor}

/**
 * @author Tobias Kohn
 */
class SymbolToken(tt: TokenType.Value, private var _text: String) extends Token(tt, _text.length) {

  override def equals(obj: Any): Boolean =
    obj match {
      case symbolToken: SymbolToken =>
        tokenType == symbolToken.tokenType && text == symbolToken.text
      case s: String =>
        s == text
      case ch: Char =>
        text.length == 1 && text(0) == ch
      case _ =>
        super.equals(obj)
    }

  def text: String = _text

  def text_=(s: String): Unit = {
    _text = s
    length = s.length
  }

  override def toString: String = f"${tokenType.toString}(`$text`)"
}

object SymbolToken {

  def unapply(token: Token): Option[(TokenType.Value, String)] =
    token match {
      case symbolToken: SymbolToken =>
        Some(symbolToken.tokenType, symbolToken.text)
      case delimiterToken: DelimiterToken =>
        Some(delimiterToken.tokenType, delimiterToken.char.toString)
      case _: ColonToken =>
        Some(TokenType.COLON, ":")
      case _ =>
        None
    }
}
