/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.struct

import tigerjython.syntaxsupport.tokens.{TokenArray, TokenType}

/**
 * @author Tobias Kohn
 */
sealed abstract class TokenChangeMessage {

  private var _handled: Boolean = false

  def handled(): Unit =
    _handled = true

  def index: Int

  def isHandled: Boolean = _handled

  def tokens: TokenArray

}

object TokenChangeMessage {

  // This is fired when a token has changed due to some smaller user input such as a user entering a number or name
  case class TokenChanged(tokens: TokenArray, index: Int) extends TokenChangeMessage

  // This is fired when a part of the text was deleted
  case class TokensDeleted(tokens: TokenArray, index: Int, delCount: Int) extends TokenChangeMessage

  // This is fired when new text was inserted
  case class TokensInserted(tokens: TokenArray, index: Int, insCount: Int) extends TokenChangeMessage {

    val (hasBrackets, hasNewlines): (Boolean, Boolean) = {
      var brackets: Boolean = false
      var newlines: Boolean = false
      for (i <- index until index + insCount)
        tokens(i).tokenType match {
          case TokenType.LEFT_BRACKET | TokenType.RIGHT_BRACKET =>
            brackets = true
          case TokenType.NEWLINE =>
            newlines = true
          case _ =>
        }
      (brackets, newlines)
    }
  }
}