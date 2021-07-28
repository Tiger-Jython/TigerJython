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
class WhitespaceToken(len: Int) extends Token(TokenType.WHITESPACE, len) {

  override def accept(document: SyntaxDocument, visitor: TokenVisitor): Unit =
    visitor.visitWhitespaceSyntaxNode(length)

  override def equals(obj: Any): Boolean =
    obj match {
      case whitespaceToken: WhitespaceToken =>
        length == whitespaceToken.length
      case _ =>
        super.equals(obj)
    }
}
