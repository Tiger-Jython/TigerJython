/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.struct

import tigerjython.syntaxsupport.tokens.TokenArray

/**
 * @author Tobias Kohn
 */
class StructBracketExpr extends StructContainer {

  length = 1

  override def getNestDepth: Int = super.getNestDepth + 1

  override protected def parseHandleRightBracket(tokens: TokenArray, index: Int): Int = -1

  override protected def parseHandleNewline(tokens: TokenArray, index: Int): Int =
    if (tokens.isStmtOnly(index + 1)) -1 else 1
}

object StructBracketExpr {

  def apply(offset: Int): StructBracketExpr = {
    val result = new StructBracketExpr()
    result.offset = offset
    result
  }
}