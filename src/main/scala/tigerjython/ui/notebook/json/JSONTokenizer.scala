/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.notebook.json

/**
 * Splits a string containing JSON into individual tokens.  This is a helper for `JSONReader`.
 */
class JSONTokenizer private (val source: CharSequence) {

  private var index: Int = 0
  private var line: Int = 1

  private def tokenize(): Array[JSONToken] = {
    val result = collection.mutable.ArrayBuffer[JSONToken]()
    while (index < source.length) {
      val ch = source.charAt(index)
      index += 1
      ch match {
        case ':' =>
          result += JSONToken.Colon(line)
        case ',' =>
          result += JSONToken.Comma(line)
        case '{' =>
          result += JSONToken.LeftBrace(line)
        case '}' =>
          result += JSONToken.RightBrace(line)
        case '[' =>
          result += JSONToken.LeftBracket(line)
        case ']' =>
          result += JSONToken.RightBracket(line)
        case '#' =>
          while (index < source.length() && source.charAt(index) != '\n')
            index += 1
        case '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | '.' =>
          result += readNumber()
        case '\"' | '\'' =>
          result += readString(ch)
        case a if a.isLetter =>
          val start = index - 1
          while (index < source.length && source.charAt(index).isLetterOrDigit)
            index += 1
          source.subSequence(start, index).toString match {
            case "False" | "false" | "FALSE" =>
              result += JSONToken.BooleanValue(false, line)
            case "True" | "true" | "TRUE" =>
              result += JSONToken.BooleanValue(true, line)
            case s =>
              result += JSONToken.Name(s, line)
          }
        case '\n' =>
          line += 1
        case _ =>
          // We simply ignore other characters and treat them as whitespace
      }
    }
    result.toArray
  }

  private def readNumber(): JSONToken = {
    val start = index - 1
    while (index < source.length && source.charAt(index).isDigit)
      index += 1
    var hasFraction =
      if (index < source.length && source.charAt(index) == '.') {
        index += 1
        while (index < source.length && source.charAt(index).isDigit)
          index += 1
        true
      } else
        source.charAt(start) == '.'
    if (index + 1 < source.length && source.charAt(index).toUpper == 'E') {
      index += 1
      if (source.charAt(index) == '+' || source.charAt(index) == '-')
        index += 1
      while (index < source.length && source.charAt(index).isDigit)
        index += 1
      hasFraction = true
    }
    val s = source.subSequence(start, index).toString
    if (hasFraction)
      JSONToken.NumberValue(s.toDouble, line)
    else
      JSONToken.IntegerValue(s.toInt, line)
  }

  private def readString(delimiter: Char): JSONToken = {
    val result = new StringBuilder()
    val curLine = line
    while (index < source.length && source.charAt(index) != delimiter)
      source.charAt(index) match {
        case '\\' if index + 1 < source.length =>
          index += 2
          source.charAt(index - 1) match {
            case 'b' => result += '\b'
            case 'f' => result += '\f'
            case 'n' => result += '\n'
            case 'r' => result += '\r'
            case 't' => result += '\t'
            case 'u' if index + 4 < source.length =>
              val i = java.lang.Integer.parseInt(source.subSequence(index, index + 4).toString, 16)
              result += i.toChar
              index += 4
            case 'x' if index + 4 < source.length =>
              val i = java.lang.Integer.parseInt(source.subSequence(index, index + 2).toString, 16)
              result += i.toChar
              index += 2
            case c   => result += c
          }
        case '\n' =>
          line += 1
          result += '\n'
          index += 1
        case c =>
          result += c
          index += 1
      }
    if (index < source.length && source.charAt(index) == delimiter)
      index += 1
    JSONToken.StringValue(result.toString(), curLine)
  }
}
object JSONTokenizer {

  /**
   * Takes a `CharSequence` containing the JSON data and returns an array of JSON tokens ready to be parsed.
   */
  def tokenize(source: CharSequence): Array[JSONToken] = {
    val tokenizer = new JSONTokenizer(source)
    tokenizer.tokenize()
  }
}