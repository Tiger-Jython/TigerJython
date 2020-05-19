/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.errorhandling

import tigerpython.parser.SyntaxChecker

/**
 * TigerJython includes a sophisticated static error checker to find errors before a script is actually executed.  This
 * is the interface running the necessary checks.
 *
 * @author Tobias Kohn
 */
object StaticErrorChecker {

  /**
   * Checks the syntax of the given program code.
   *
   * If an error has been found, the function returns a triple `(line, offset, message)` indicating the first error
   * that was detected.
   *
   * @param filename     The name of the file---sometimes used for generating error messages.
   * @param programCode  The source code of the Python program to check.
   * @return             If an error has been found, its location (line, offset) and the error message is reported.
   *                     If no error has been found, `None`.
   */
  def checkSyntax(filename: String, programCode: String): Option[(Int, Int, String)] = {
    val syntaxChecker = new SyntaxChecker(programCode, filename)
    syntaxChecker.check() match {
      case Some((pos, msg)) =>
        val line = syntaxChecker.lineFromPosition(pos)
        val offs = syntaxChecker.lineOffsetFromPosition(pos)
        Some((line, offs, msg))
      case None =>
        None
    }
  }
}
