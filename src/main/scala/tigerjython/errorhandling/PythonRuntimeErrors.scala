/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.errorhandling

/**
 * This is the interface for parsing runtime error messages (tracebacks) as reported by Python.
 *
 * @author Tobias Kohn
 */
object PythonRuntimeErrors {

  private def parseLocationInfo(s: String): (Int, String) = {
    println("?", s)
    if (s.startsWith("File \"")) {
      val filename = s.drop(6).takeWhile(_ != '\"')
      val rest = s.drop(9 + filename.length)
      if (rest.startsWith("line ")) {
        val line: Int = rest.drop(5).takeWhile(_.isDigit).toInt
        return (line, filename)
      }
    }
    (-1, null)
  }

  def generateMessage(originalMessage: String): (Int, String, String) = {
    val lines = originalMessage.split('\n').map(_.filter(_ >= ' '))
    if (lines.length >= 3 && lines.head == "Traceback (most recent call last):") {
      var infoLine: String = null
      var i = lines.length-2
      while (i > 0 && infoLine == null) {
        val s = lines(i).trim
        if (s.startsWith("File \""))
          infoLine = s
        else
          i -= 1
      }
      if (infoLine != null) {
        val (line, filename) = parseLocationInfo(infoLine)
        (line, filename, lines.last)
      } else
        (-1, null, null)
    } else
    if (lines.length >= 3 && lines.head.trim.startsWith("File ")) {
      val (line, filename) = parseLocationInfo(lines.head.trim)
      (line, filename, lines.last)
    } else
      (-1, null, null)
  }
}
