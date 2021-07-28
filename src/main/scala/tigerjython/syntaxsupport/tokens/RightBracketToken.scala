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
class RightBracketToken(val bracket: Char) extends Token(TokenType.RIGHT_BRACKET, 1) {

  private[syntaxsupport] var _isApplication: Boolean = false
  private[syntaxsupport] var _isData: Boolean = bracket == '}'

  override def equals(obj: Any): Boolean =
    obj match {
      case rightBracketToken: RightBracketToken =>
        bracket == rightBracketToken.bracket
      case ch: Char =>
        ch == bracket
      case _ =>
        super.equals(obj)
    }

  def isApplication: Boolean = _isApplication

  def isData: Boolean = _isData

  override def matches(other: Token): Boolean =
    other match {
      case leftBracketToken: LeftBracketToken =>
        val left = leftBracketToken.bracket
        (left == '(' && bracket == ')') || (left == '[' && bracket == ']') || (left == '{' && bracket == '}')
      case _ =>
        false
    }

  override def toString: String = f"right-bracket(`$bracket`)"
}

object RightBracketToken {
  def unapply(arg: RightBracketToken): Option[Char] =
    if (arg != null)
      Some(arg.bracket)
    else
      None
}