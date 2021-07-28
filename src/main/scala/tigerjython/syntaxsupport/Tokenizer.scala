/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport

import tigerjython.syntaxsupport.tokens.Token

/**
 * @author Tobias Kohn
 */
trait Tokenizer extends Iterator[Token] {

  def extendParseRange(length: Int): Unit

  def seek(position: Int): Int

  def setParseRange(startPosition: Int, endPosition: Int): Unit
}
