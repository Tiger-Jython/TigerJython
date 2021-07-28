/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.parser

import tigerjython.syntaxsupport.SyntaxDocument
import tigerjython.syntaxsupport.struct.StructElement

/**
 * @author Tobias Kohn
 */
trait StmtParser {

  def parse(structElement: StructElement): StatementType
}
object StmtParser {

  def forDocument(document: SyntaxDocument): StmtParser =
    if (document != null)
      new PythonStmtParser(document)
    else
      null
}