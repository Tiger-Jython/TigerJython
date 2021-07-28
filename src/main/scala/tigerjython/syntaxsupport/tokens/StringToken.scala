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
class StringToken(val text: String) extends Token(TokenType.STRING_LITERAL, text.length) {

  private val spans: Array[Int] = {
    val result = collection.mutable.ArrayBuffer[Int]()
    var i = 0
    while (i < text.length) {
      val j = text.indexOf('\\', i)
      if (j >= 0) {
        result += j - i
        result += 2
        i = j + 2
      } else {
        result += text.length - i
        i = text.length
      }
    }
    result.toArray
  }

  override def accept(document: SyntaxDocument, visitor: TokenVisitor): Unit = {
    /*var escape: Boolean = false
    for (span <- spans) {
      if (escape)
        visitor.visitSyntaxNode("string-escape", span)
      else
        visitor.visitSyntaxNode("string", span)
      escape = !escape
    }*/
    visitor.visitSyntaxNode("string", length)
  }
}
