/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.parser

import tigerjython.syntaxsupport.struct.StructElement
import tigerjython.syntaxsupport.tokens._

import scala.collection.BufferedIterator

/**
 * @author Tobias Kohn
 */
class TokenSource(val source: TokenArray, val structElement: StructElement) extends BufferedIterator[Token] {

  protected val tokens: Array[Token] =
    if (structElement != null && structElement.length > 0) {
      val idx = structElement.index
      val result = collection.mutable.ArrayBuffer[Token]()
      for (i <- idx until idx + structElement.length) {
        val tkn = source(i)
        if (tkn != null && tkn.tokenType != TokenType.WHITESPACE && tkn.tokenType != TokenType.COMMENT &&
          tkn.tokenType != TokenType.NEWLINE)
          result += tkn
      }
      result.toArray
    } else
      Array()

  private var _index: Int = 0

  def back(): Unit =
    if (_index > 0)
      _index -= 1

  def expect(tokenTypes: TokenType.Value*): Boolean =
    if (tokenTypes.contains(headTokenType)) {
      skip()
      true
    } else
      false

  def expectBracket(bracket: Char): Boolean =
    head match {
      case LeftBracketToken(b) if b == bracket =>
        skip()
        true
      case RightBracketToken(b) if b == bracket =>
        skip()
        true
      case _ =>
        false
    }

  def expectColon(): Boolean =
    if (headTokenType == TokenType.COLON) {
      skip()
      true
    } else
      false

  def expectComma(): Boolean =
    if (headTokenType == TokenType.COMMA) {
      skip()
      true
    } else
      false

  def expectKeyword(keyword: String): Boolean =
    head match {
      case NameToken(TokenType.KEYWORD | TokenType.DEF_KEYWORD | TokenType.CLASS_KEYWORD | TokenType.LAMBDA, _, s)
        if keyword == s =>
        skip()
        true
      case _ =>
        false
    }

  def expectName(): String =
    head match {
      case NameToken(TokenType.NAME | TokenType.ATTRIBUTE | TokenType.BUILTIN_NAME, _, s) =>
        skip()
        s
      case _ =>
        null
    }

  def expectName(nameTokenType: NameTokenType.Value): String =
    head match {
      case n @ NameToken(TokenType.NAME | TokenType.ATTRIBUTE | TokenType.BUILTIN_NAME, _, s) =>
        n.nameTokenType = nameTokenType
        skip()
        s
      case _ =>
        null
    }

  def expectRightBracket(leftBracketToken: LeftBracketToken = null): Boolean =
    head match {
      case right: RightBracketToken =>
        if (leftBracketToken != null && leftBracketToken.matches(right))
          right._isData = leftBracketToken._isData
        else
          right._isData = right.bracket == '}'
        skip()
        true
      case _ =>
        false
    }

  def expectStar(): Boolean =
    head match {
      case SymbolToken(TokenType.SYMBOL, "*") =>
        skip()
        true
      case _ =>
        false
    }

  def expectSymbol(sym: String): Boolean =
    head match {
      case SymbolToken(_, s) if s == sym =>
        skip()
        true
      case _ =>
        false
    }

  def hasNext: Boolean =
    head != null

  def hasTokenType(tokenTypes: TokenType.Value*): Boolean =
    tokenTypes.contains(headTokenType)

  def head: Token =
    if (_index < tokens.length)
      tokens(_index)
    else
      null

  def headTokenType: TokenType.Value = {
    val hd = head
    if (hd != null)
      hd.tokenType
    else
      TokenType.NONE
  }

  def ignore(tokenTypes: TokenType.Value*): Unit = {
    while (tokenTypes.contains(headTokenType))
      skip()
  }

  def ignoreAllUntil(tokenTypes: TokenType.Value*): Unit =
    while (_index < tokens.length && !tokenTypes.contains(headTokenType))
      skip()

  def index: Int = _index

  def markUnknownNames(nameTokenType: NameTokenType.Value): Unit =
    for (tkn <- tokens)
      tkn match {
        case n: NameToken if n.isName =>
          if (n.nameTokenType == NameTokenType.UNKNOWN)
            n.nameTokenType = nameTokenType
        case _ =>
      }

  def markUnknownNamesUntil(nameTokenType: NameTokenType.Value, index: Int): Unit =
    for (i <- 0 until (index min tokens.length))
      tokens(i) match {
        case n: NameToken if n.isName =>
          if (n.nameTokenType == NameTokenType.UNKNOWN)
            n.nameTokenType = nameTokenType
        case _ =>
      }

  def next(): Token =
    if (_index < tokens.length) {
      val result = tokens(_index)
      _index += 1
      result
    } else
      null

  def peek(relIndex: Int = 0): Token = {
    val idx = _index + relIndex
    if (0 <= idx && idx < tokens.length)
      tokens(idx)
    else
      null
  }

  def prev: Token =
    if (_index > 0)
      tokens(_index - 1)
    else
      null

  def skip(delta: Int = 1): Unit =
    _index = (_index + delta) min tokens.length
}
