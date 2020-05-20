/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.config

/**
 * The `TokenSource` is a `Token`-iterator that takes a text as a char-sequence as input and splits it up into tokens.
 *
 * @author Tobias Kohn
 */
class TokenSource(val source: CharSequence) extends Iterator[Token] {

  protected var index: Int = 0

  private var _cache: Token = _

  protected def cache: Token = {
    if (_cache == null)
      _cache = readNextToken()
    _cache
  }

  def hasNext: Boolean =
    cache != null

  def next(): Token = {
    val result = cache
    _cache = null
    result
  }

  def peek(): Token = cache

  def skipLine(): Unit = {
    while (index < source.length && source.charAt(index) != '\n')
      index += 1
    _cache = readNextToken()
  }

  protected def readNextToken(): Token = {
    while (index < source.length)
      source.charAt(index) match {
        case '#' | '%' | ';' =>   // (line= comments
          while (index < source.length && source.charAt(index) != '\n')
            index += 1
        case '/' if index+1 < source.length && source.charAt(index+1) == '/' =>
          while (index < source.length && source.charAt(index) != '\n')
            index += 1
        case x if x <= ' ' =>     // whitespace
          index += 1
        case '+' | '-' | '.' if index+1 < source.length && source.charAt(index+1).isDigit =>
          return Token.VALUE(readNumber())
        case '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' =>
          return Token.VALUE(readNumber())
        case '.' =>
          index += 1
          return Token.DOT
        case '+' | '&' =>
          index += 1
          return Token.PLUS
        case ch @ ('"' | '\'') =>
          index += 1
          return Token.VALUE(readString(ch))
        case '{' =>
          index += 1
          return Token.LEFT_BRACE
        case '}' =>
          index += 1
          return Token.RIGHT_BRACE
        case ':' =>
          if (index+1 < source.length && source.charAt(index+1) == '=')
            index += 2
          else
            index += 1
          return Token.ASSIGNMENT
        case '=' =>
          index += 1
          return Token.ASSIGNMENT
        case '_' | '$' =>
          return Token.NAME(readName())
        case x if x.isLetter =>
          val s = readName()
          if (s.toLowerCase == "true" || s.toLowerCase == "false")
            return Token.VALUE(s)
          else
            return Token.NAME(s)
        case _ =>
          index += 1
      }
    null
  }

  final private def isHexDigit(ch: Char): Boolean =
    ch.isDigit || ('A' <= ch && ch <= 'F') || ('a' <= ch && ch <= 'f')

  final private def isLetter(ch: Char): Boolean =
    ch.isLetterOrDigit || ch == '_' || ch == '$'

  protected def readName(): String = {
    val start = index
    while (index < source.length && isLetter(source.charAt(index)))
      index += 1
    source.subSequence(start, index).toString
  }

  protected def readNumber(): String = {
    val start = index
    val isNegative = source.charAt(index) == '-'
    if (isNegative || source.charAt(index) == '+')
      index += 1
    if (index + 3 < source.length && source.charAt(index) == '0' && source.charAt(index+1) == 'x' &&
        isHexDigit(source.charAt(index+2))) {
        index += 2
        var result = 0
        while (index < source.length && isHexDigit(source.charAt(index))) {
          result *= 0x10
          source.charAt(index) match {
            case ch @ ('0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9') =>
              result += ch - '0'
            case ch @ ('A' | 'B' | 'C' | 'D' | 'E' | 'F') =>
              result += 10 + ch - 'A'
            case ch @ ('a' | 'b' | 'c' | 'd' | 'e' | 'f') =>
              result += 10 + ch - 'a'
            case _ =>
          }
        }
        if (isNegative)
          return "-" + result.toString
        else
          return result.toString
      }
    while (index < source.length && source.charAt(index).isDigit)
      index += 1
    if (index+1 < source.length && source.charAt(index) == '.' && source.charAt(index+1).isDigit) {
      index += 1
      while (index < source.length && source.charAt(index).isDigit)
        index += 1
    }
    if (index+2 < source.length && source.charAt(index).toLower == 'e' &&
      (source.charAt(index+1).isDigit ||
        ((source.charAt(index+1) == '+' || source.charAt(index+1) == '-') && source.charAt(index+2).isDigit))) {
      index += 2
      while (index < source.length && source.charAt(index).isDigit)
        index += 1
    }
    source.subSequence(start, index).toString
  }

  protected def readString(delimiter: Char): String = {
    val result = new StringBuilder()
    while (index < source.length && source.charAt(index) != delimiter)
      source.charAt(index) match {
        case '\\' if index+1 < source.length =>
          source.charAt(index+1) match {
            case 'n' =>
              result += '\n'
            case 't' =>
              result += '\t'
            case ch @ ('\"' | '\'' | '\\') =>
              result += ch
            case ch =>
              result += '\\'
              result += ch
          }
          index += 2
        case x =>
          result += x
          index += 1
      }
    if (index < source.length && source.charAt(index) == delimiter)
      index += 1
    result.toString()
  }
}
