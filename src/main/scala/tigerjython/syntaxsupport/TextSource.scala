/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport

import scala.collection.BufferedIterator

/**
 * The text-source is an iterator with a series of additional helper methods and functions that allow you to recognise
 * whether a character is a hexadecimal digit, a letter, etc.
 *
 * @author Tobias Kohn
 */
class TextSource(val source: StringBuilder) extends BufferedIterator[Char] {

  protected var index: Int = 0

  def apply(relIndex: Int): Char = {
    val idx = index + relIndex
    if (0 <= idx && idx < source.length())
      source.charAt(idx)
    else
      '\u0000'
  }

  def consume(count: Int = 1): Int = {
    val idx = index
    index = (idx + count) min source.length()
    index - idx
  }

  def consumeIntegerPart(): Int = {
    def _isDigit(i: Int): Boolean = {
      val ch = source.charAt(i)
      if (ch == '_' && i + 1 < source.length())
        source.charAt(i + 1).isDigit
      else
        ch.isDigit
    }

    var i = index
    if (i < source.length()) {
      val ch = source.charAt(i)
      if (ch == '.' || ch == '+' || ch == '-')
        i += 1
    }
    while (i < source.length() && _isDigit(i))
      i += 1
    val result = i - index
    index = i
    result
  }

  def consumeLineComment(): Int = {
    var i = index
    while (i < source.length() && source.charAt(i) != '\n')
      i += 1
    val result = i - index
    index = i
    result
  }

  def consumeName(): Int = {
    def _isLetterOrDigit(ch: Char): Boolean =
      ch.isLetterOrDigit || ch == '_' || (ch > 0xFF && ch.isUnicodeIdentifierPart)

    var i = index
    while (i < source.length() && _isLetterOrDigit(source.charAt(i)))
      i += 1
    val result = i - index
    index = i
    result
  }

  def consumeNewline(): Int =
    if (0 <= index && index < source.length()) {
      val ch = source.charAt(index)
      val result =
        if (ch == '\r' && index+1 < source.length && source.charAt(index+1) == '\n')
          2
        else if (ch == '\n' || ch == '\r')
          1
        else
          0
      index += result
      result
    } else
      0

  def consumeAndReturnString(count: Int = 1): String = {
    val idx = index
    index = (idx + count) min source.length()
    source.substring(idx, index)
  }

  def consumeWhitespace(): Int = {
    def _isWhitespace(ch: Char): Boolean =
      ch == ' ' || ch == '\t' || ch == '\f'

    var i = index
    while (i < source.length() && _isWhitespace(source.charAt(i)))
      i += 1
    val result = i - index
    index = i
    result
  }

  def current: Char = apply(0)

  def currentPosition: Int = index

  protected[syntaxsupport]
  def getConsumedName(len: Int): String =
    source.substring(index - len, index)

  def hasNext: Boolean = index < source.length()

  def hasPrecedingDot(relIndex: Int = 0): Boolean = {
    var idx = index + relIndex
    while (idx > 0 && ((ch: Char) => ch == ' ' || ch == '\t')(source.charAt(idx - 1)))
      idx -= 1
    idx > 0 && source.charAt(idx) == '.'
  }

  def head: Char = apply(0)

  /**
   * Returns `true` if the given position is at the start of a line (not counting any whitespace characters).
   */
  def isAtStartOfLine(relIndex: Int = 0): Boolean = {
    var idx = index + relIndex
    while (idx > 0 && ((ch: Char) => ch == ' ' || ch == '\t')(source.charAt(idx - 1)))
      idx -= 1
    idx == 0 || ((ch: Char) => ch == '\n' || ch == '\r')(source.charAt(idx - 1))
  }

  def isBinDigit(relIndex: Int = 0): Boolean = {
    var ch = apply(relIndex)
    if (ch == '_')
      ch = apply(relIndex + 1)
    ch == '0' || ch == '1'
  }

  def isDigit(relIndex: Int = 0): Boolean = {
    val ch = apply(relIndex)
    ch.isDigit || (ch == '_' && apply(relIndex+1).isDigit)
  }

  def isDouble(relIndex: Int = 0): Boolean = {
    val idx = (index + relIndex) max 0
    (idx + 1 < source.length()) && (source.charAt(idx) == source.charAt(idx + 1))
  }

  def isDoubleAugAssignment(relIndex: Int = 0): Boolean = {
    val idx = (index + relIndex) max 0
    (idx + 2 < source.length) && (source.charAt(idx) == source.charAt(idx + 1)) && (source.charAt(idx + 2) == '=')
  }

  def isEqualTo(s: String): Boolean = isEqualTo(0, s)

  def isEqualTo(relIndex: Int, s: String): Boolean = {
    val idx = index + relIndex
    if (0 <= idx && idx + s.length < source.length()) {
      var i = 0
      while (i < s.length) {
        if (s.charAt(i) != source.charAt(idx + i))
          return false
        i += 1
      }
      true
    } else
      false
  }

  def isHexDigit(relIndex: Int = 0): Boolean = {
    var ch = apply(relIndex)
    if (ch == '_')
      ch = apply(relIndex + 1)
    ch.isDigit || ('A' <= ch && ch <= 'F') || ('a' <= ch && ch <= 'f')
  }

  def isLeftBracket(relIndex: Int = 0): Boolean = {
    val ch = apply(relIndex)
    ch == '(' || ch == '[' || ch == '{'
  }

  def isLetter(relIndex: Int = 0): Boolean = {
    val ch = apply(relIndex)
    ch.isLetter || (ch == '_') || (ch >= 0xFF && ch.isUnicodeIdentifierStart)
  }

  def isLetterOrDigit(relIndex: Int = 0): Boolean = {
    val ch = apply(relIndex)
    ch.isLetterOrDigit || (ch == '_') || (ch >= 0xFF && ch.isUnicodeIdentifierPart)
  }

  def isOctDigit(relIndex: Int = 0): Boolean = {
    var ch = apply(relIndex)
    if (ch == '_')
      ch = apply(relIndex + 1)
    '0' <= ch && ch <= '7'
  }

  def isOneOf(relIndex: Int, chars: Char*): Boolean =
    chars.contains(apply(relIndex))

  def isRightBracket(relIndex: Int = 0): Boolean = {
    val ch = apply(relIndex)
    ch == ')' || ch == ']' || ch == '}'
  }

  def isSign(relIndex: Int = 0): Boolean = {
    val ch = apply(relIndex)
    ch == '+' || ch == '-'
  }

  def isSignedIntegerStart(relIndex: Int = 0): Boolean = {
    var ch = apply(relIndex)
    if (ch == '+' || ch == '-')
      ch = apply(relIndex + 1)
    ch.isDigit
  }

  def isTriple(relIndex: Int = 0): Boolean = {
    val idx = (index + relIndex) max 0
    if (idx + 2 < source.length) {
      val ch = source.charAt(idx)
      ch == source.charAt(idx + 1) && ch == source.charAt(idx + 2)
    } else
      false
  }

  override def knownSize: Int = source.length()

  def next(): Char =
    if (index < source.length()) {
      val result = source.charAt(index)
      index += 1
      result
    } else
      Iterator.empty.next()

  def peek(relIndex: Int = 0): Char = apply(relIndex)

  def prefixLength(p: Char=>Boolean): Int = {
    var i = index
    while (i < source.length && p(source.charAt(i)))
      i += 1
    i - index
  }

  def remaining: Int = source.length - index

  def repetitionCount(relIndex: Int = 0): Int = {
    val ch = apply(relIndex)
    if (ch > 0x00) {
      var i = index + relIndex
      while (i < source.length && source.charAt(i) == ch)
        i += 1
      i - (index + relIndex)
    } else
      0
  }

  def seek(position: Int): Int = {
    index = (position max 0) min source.length()
    index
  }

  def subString(len: Int): String = {
    val end = (index + len) min source.length()
    source.substring(index, end)
  }

  def subString(relIndex: Int, len: Int): String = {
    val idx = index + relIndex
    if (0 <= idx && idx < source.length()) {
      val end = (idx + len) min source.length()
      source.substring(idx, end)
    } else
      null
  }

  def textLength: Int = source.length()
}
