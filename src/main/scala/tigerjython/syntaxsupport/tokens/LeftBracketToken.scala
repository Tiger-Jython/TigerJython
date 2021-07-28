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
class LeftBracketToken(val bracket: Char) extends Token(TokenType.LEFT_BRACKET, 1) {

  private[syntaxsupport] var _isApplication: Boolean = false
  private[syntaxsupport] var _isData: Boolean = bracket == '{'

  override def equals(obj: Any): Boolean =
    obj match {
      case leftBracketToken: LeftBracketToken =>
        bracket == leftBracketToken.bracket
      case ch: Char =>
        ch == bracket
      case _ =>
        super.equals(obj)
    }

  def isApplication: Boolean = _isApplication

  def isData: Boolean = _isData

  override def matches(other: Token): Boolean =
    other match {
      case rightBracketToken: RightBracketToken =>
        val right = rightBracketToken.bracket
        (bracket == '(' && right == ')') || (bracket == '[' && right == ']') || (bracket == '{' && right == '}')
      case _ =>
        false
    }

  override def toString: String = f"left-bracket(`$bracket`)"
}

object LeftBracketToken {
  def unapply(arg: LeftBracketToken): Option[Char] =
    if (arg != null)
      Some(arg.bracket)
    else
      None
}