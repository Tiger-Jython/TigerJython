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
class ColonToken extends Token(TokenType.COLON, 1) {

  private var _isCompoundColon: Boolean = false

  override def accept(document: SyntaxDocument, visitor: TokenVisitor): Unit =
    if (isCompoundColon)
      visitor.visitSyntaxNode("colon", length)
    else
      visitor.visitSyntaxNode(null, length)

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

  def setIsCompoundColon(c: Boolean): Unit =
    _isCompoundColon = c

  def text: String = ":"

  override def toString: String = "colon"
}
