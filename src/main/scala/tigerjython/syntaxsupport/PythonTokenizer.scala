/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport

import tigerjython.syntaxsupport.tokens.{Token, TokenType}

/**
 * @author Tobias Kohn
 */
class PythonTokenizer(val document: SyntaxDocument) extends Tokenizer {

  protected val source: TextSource = new TextSource(document.text)
  private var endPosition: Int = -1

  private val builtinNames = Set[String](
    "abs", "all", "any", "ascii", "bin", "bool", "breakpoint", "bytearray", "bytes", "callable", "chr",
    "classmethod", "compile", "complex", "delattr", "dict", "dir", "divmod", "enumerate", "eval", "exec",
    "filter", "float", "format", "frozenset", "getattr", "globals", "hasattr", "hash", "help", "hex", "id",
    "input", "int", "isinstance", "issubclass", "iter", "len", "list", "locals", "map", "max", "memoryview",
    "min", "next", "object", "oct", "open", "ord", "pow", "print", "property", "range", "repr", "reversed",
    "round", "set", "setattr", "slice", "sorted", "staticmethod", "str", "sum", "super", "tuple", "type",
    "vars", "zip",
    "self"
  )

  private val keywords = Set[String](
    "False", "None", "True",
    "and", "as", "assert", "async", "await", "break", "class", "continue", "def", "del", "elif", "else",
    "except", "finally", "for", "from", "global", "if", "import", "in", "is", "lambda", "nonlocal", "not", "or",
    "pass", "raise", "return", "try", "while", "with", "yield"
  )

  // These are keywords that cannot occur inside brackets or parentheses, as they always start a statement
  private val stmtOnlyKeywords = Set[String](
    "class", "def", "del", "elif", "except", "finally", "global", "import", "nonlocal", "pass",
    "raise", "return", "try", "while", "with"
  )

  var repeatIsKeyword: Boolean = true

  def extendParseRange(length: Int): Unit =
    if (length > 0) {
      if (endPosition != -1)
        endPosition = (endPosition + length) min source.length
      else
        endPosition = length min source.length
    }

  def hasNext: Boolean = source.hasNext && (endPosition < 0 || source.currentPosition < endPosition)

  def isBuiltinName(s: String): Boolean =
    builtinNames.contains(s) || (s.length > 4 && s.startsWith("__") && s.endsWith("__"))

  def isKeyword(s: String): Boolean = keywords.contains(s)

  def isStmtOnlyKeyword(s: String): Boolean = stmtOnlyKeywords.contains(s)

  def next(): Token =
    source.current match {
      case '#' =>
        Token(TokenType.COMMENT, source.consumeLineComment())
      case '\n' | '\r' =>
        Token(TokenType.NEWLINE, source.consumeNewline())
      case ' ' | '\t' | '\f' =>
        Token(TokenType.WHITESPACE, source.consumeWhitespace())
      case '-' if source(1) == '>' =>
        Token(TokenType.SYMBOL, source.consumeAndReturnString(2))
      case '+' | '-' =>
        if (source.isSignedIntegerStart())
          readNumber()
        else if (source(1) == '=')
          Token(TokenType.ASSIGNMENT, source.consumeAndReturnString(2))
        else
          Token(TokenType.SYMBOL, source.consumeAndReturnString())
      case '\"' | '\'' =>
        readStringLiteral()
      case ch @ ('(' | '[') =>
        source.consume()
        Token(ch)
      case ch @ (')' | ']') =>
        source.consume()
        Token(ch)
      case '{' =>
        source.consume()
        Token('{')
      case '}' =>
        source.consume()
        Token('}')
      case '.' =>
        if (source.isDigit(1))
          readNumber()
        else if (source.isTriple())
          Token(TokenType.ELLIPSIS, source.consumeAndReturnString(3))
        else
          Token(TokenType.DOT, source.consumeAndReturnString())
      case ':' =>
        if (source(1) == '=')
          Token(TokenType.EXPR_ASSIGN, source.consume(2))
        else
          Token(TokenType.COLON, source.consume())
      case ch @ (',' | ';') =>
        source.consume()
        Token(ch)
      case '=' | '<' | '>' | '!' if source(1) == '=' =>
        Token(TokenType.COMPARATOR, source.consumeAndReturnString(2))
      case '=' =>
        Token(TokenType.ASSIGNMENT, source.consumeAndReturnString())
      case '<' if source(1) == '>' =>
        Token(TokenType.COMPARATOR, source.consumeAndReturnString(2))
      case '<' | '>' =>
        if (source.isDouble()) {
          if (source(2) == '=')
            Token(TokenType.ASSIGNMENT, source.consumeAndReturnString(3))
          else
            Token(TokenType.SYMBOL, source.consumeAndReturnString(2))
        } else
          Token(TokenType.COMPARATOR, source.consumeAndReturnString())
      case '*' | '/' | '^' | '%' =>
        if (source.isDouble()) {
          if (source(2) == '=')
            Token(TokenType.ASSIGNMENT, source.consumeAndReturnString(3))
          else
            Token(TokenType.SYMBOL, source.consumeAndReturnString(2))
        } else {
          if (source(1) == '=')
            Token(TokenType.ASSIGNMENT, source.consumeAndReturnString(2))
          else
            Token(TokenType.SYMBOL, source.consumeAndReturnString())
        }
      case '&' | '|' | '~' | '@' | '!' =>
        if (source(1) == '=')
          Token(TokenType.ASSIGNMENT, source.consumeAndReturnString(2))
        else
          Token(TokenType.SYMBOL, source.consumeAndReturnString())
      case '$' | '?' | '`' =>
        Token(TokenType.INVALID, source.consume())
      case '\\' =>
        source.consume()
        val len = source.consumeNewline()
        if (len > 0)
          Token(TokenType.WHITESPACE, len + 1)
        else
          Token(TokenType.INVALID, 1)
      case 'b' | 'B' | 'f' | 'F' =>
        if (source.isOneOf(1, '\"', '\''))
          readStringLiteral()
        else if (source.isOneOf(1, 'r', 'R') && source.isOneOf(2, '\"', '\''))
          readStringLiteral()
        else
          readName()
      case 'r' | 'R' =>
        if (source.isOneOf(1, '\"', '\''))
          readStringLiteral()
        else if (source.isOneOf(1, 'b', 'B', 'f', 'F') && source.isOneOf(2, '\"', '\''))
          readStringLiteral()
        else
          readName()
      case 'u' | 'U' if source.isOneOf(1, '\"', '\'') =>
        readStringLiteral()
      case _ =>
        if (source.isDigit())
          readNumber()
        else if (source.isLetter())
          readName()
        else
          Token(TokenType.SYMBOL, source.consumeAndReturnString())
    }

  protected def readName(): Token = {
    val len = source.consumeName()
    val s = source.getConsumedName(len)
    if (s == "def")
      Token(TokenType.DEF_KEYWORD, len)
    else if (s == "class")
      Token(TokenType.CLASS_KEYWORD, len)
    else if (s == "lambda")
      Token(TokenType.LAMBDA, len)
    else if (s == "repeat" && repeatIsKeyword)
      Token(TokenType.KEYWORD, s, source.isAtStartOfLine(-len))
    else if (isStmtOnlyKeyword(s))
      Token(TokenType.KEYWORD, s, source.isAtStartOfLine(-len))
    else if (isKeyword(s))
      Token(TokenType.KEYWORD, s)
    else if (source.hasPrecedingDot(-len))
      Token(TokenType.ATTRIBUTE, s)
    else if (isBuiltinName(s))
      Token(TokenType.BUILTIN_NAME, s)
    else
      Token(TokenType.NAME, s)
  }

  def getTokenTypeForName(s: String): TokenType.Value = {
    if (s == "def")
      TokenType.DEF_KEYWORD
    else if (s == "class")
      TokenType.CLASS_KEYWORD
    else if (s == "lambda")
      TokenType.LAMBDA
    else if (s == "repeat" && repeatIsKeyword)
      TokenType.KEYWORD
    else if (isKeyword(s))
      TokenType.KEYWORD
    else if (isBuiltinName(s))
      TokenType.BUILTIN_NAME
    else
      TokenType.NAME
  }

  protected def readNumber(): Token = {
    val start = source.currentPosition
    if (source.isSign())
      source.consume()
    if (source.current == '0' && source.isOneOf(1, 'X', 'x', 'O', 'o', 'B', 'b')) {
      source.consume(2)
      source(-1) match {
        case 'B' | 'b' =>
          while (source.isBinDigit())
            source.consume()
        case 'O' | 'o' =>
          while (source.isOctDigit())
            source.consume()
        case 'X' | 'x' =>
          while (source.isHexDigit())
            source.consume()
        case _ =>
      }
    } else {
      source.consumeIntegerPart()
      if (source.current == '.' && source.isDigit(1))
        source.consumeIntegerPart()
      if (source.isOneOf(0, 'E', 'e') && source.isSignedIntegerStart(1)) {
        source.consume()
        source.consumeIntegerPart()
      }
    }
    Token(TokenType.NUMBER, source.currentPosition - start)
  }

  private def _readString(delimiter: Char): Unit =
    while (source.hasNext)
      source.next() match {
        case ch @ ('\'' | '\"') if ch == delimiter =>
          return
        case '\\' =>
          source.consume()
        case '\n' | '\r' =>
          source.consume(-1)
          return
        case _ =>
      }

  private def _readStringMulti(delimiter: Char): Unit =
    while (source.hasNext)
      source.next() match {
        case ch @ ('\'' | '\"') if ch == delimiter && source.isTriple(-1) =>
          source.consume(2)
          return
        case '\\' =>
          source.consume()
        case _ =>
      }

  protected def readStringLiteral(): Token =
    if (source.isOneOf(0, 'f', 'F') || (source.isLetter() && source.isOneOf(1, 'f', 'F'))) {
      // Read an F-String...
      null
    } else {
      val start = source.currentPosition
      source.consumeName()            // Skip any prefix
      val delimiter = source.current
      if (source.isTriple()) {
        // Multiline string literal
        source.consume(2)
        _readStringMulti(delimiter)
      } else {
        // Single-line string literal
        source.consume()
        _readString(delimiter)
      }
      val len = source.currentPosition - start
      Token(TokenType.STRING_LITERAL, source.subString(-len, len))
    }

  def seek(position: Int): Int =
    source.seek(position)

  def setParseRange(startPosition: Int, endPosition: Int): Unit = {
    source.seek(startPosition)
    if (endPosition == -1)
      this.endPosition = -1
    else
      this.endPosition = endPosition max startPosition
  }
}
