/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.tokens

import tigerjython.syntaxsupport.{SyntaxDocument, TokenVisitor}
import tigerjython.syntaxsupport.struct.StructElement

/**
 * @author Tobias Kohn
 */
class Token (var tokenType: TokenType.Value, len: Int) {

  private var _length: Int = len

  var nested: Boolean = false
  var structElement: StructElement = _

  def length: Int = _length
  def length_=(len: Int): Unit = setLength(len)

  def accept(document: SyntaxDocument, visitor: TokenVisitor): Unit =
    tokenType match {
      case TokenType.COMMENT =>
        visitor.visitSyntaxNode("comment", length)
      case TokenType.KEYWORD | TokenType.DEF_KEYWORD | TokenType.CLASS_KEYWORD =>
        visitor.visitSyntaxNode("keyword", length)
      case TokenType.STRING_LITERAL =>
        visitor.visitSyntaxNode("string", length)
      case _ =>
        visitor.visitSyntaxNode(tokenType.toString, length)
    }

  override def equals(obj: Any): Boolean =
    obj match {
      case token: Token if tokenType == token.tokenType && length == token.length =>
        tokenType == TokenType.NEWLINE || tokenType == TokenType.COLON || tokenType == TokenType.WHITESPACE
      case tokenType: TokenType.Value =>
        this.tokenType == tokenType
      case _ =>
        super.equals(obj)
    }

  def matches(other: Token): Boolean = false

  protected def setLength(len: Int): Unit =
    if (len >= 0)
      _length = len

  override def toString: String = f"${tokenType.toString}($length)"
}

object Token {

  def apply(ch: Char): Token =
    ch match {
      case '(' | '[' | '{' =>
        new LeftBracketToken(ch)
      case ')' | ']' | '}' =>
        new RightBracketToken(ch)
      case ';' | ',' =>
        new DelimiterToken(ch)
      case ':' =>
        new ColonToken()
      case '=' =>
        apply(TokenType.ASSIGNMENT, "=")
      case ' ' | '\t' =>
        apply(TokenType.WHITESPACE, 1)
      case '_' =>
        apply(TokenType.NAME, "_")
      case '<' | '>' =>
        apply(TokenType.COMPARATOR, ch.toString)
      case '#' =>
        apply(TokenType.COMMENT, 1)
      case '$' | '?' | '`' | '!' =>
        apply(TokenType.INVALID, 1)
      case '.' =>
        apply(TokenType.DOT, ".")
      case '@' | '+' | '-' | '*' | '/' | '%' | '~' =>
        apply(TokenType.SYMBOL, ch.toString)
      case '\n' =>
        apply(TokenType.NEWLINE, 1)
      case _ =>
        if (ch.isDigit)
          apply(TokenType.NUMBER, 1)
        else if (ch.isLetter)
          apply(TokenType.NAME, ch.toString)
        else
          apply(TokenType.NONE, 1)
    }

  def apply(tokenType: TokenType.Value, text: String): Token =
    tokenType match {
      case TokenType.ASSIGNMENT | TokenType.SYMBOL | TokenType.COMPARATOR | TokenType.DOT | TokenType.ELLIPSIS =>
        new SymbolToken(tokenType, text)
      case TokenType.STRING_LITERAL =>
        new StringToken(text)
      case _ =>
        val result = new NameToken(tokenType, text)
        tokenType match {
          case TokenType.DEF_KEYWORD | TokenType.CLASS_KEYWORD =>
            result.setStmtOnlyKeyword(true)
          case _ =>
        }
        result
    }

  def apply(tokenType: TokenType.Value, text: String, isStmtOnlyKeyword: Boolean): Token = {
    val result = new NameToken(tokenType, text)
    result.setStmtOnlyKeyword(isStmtOnlyKeyword)
    result
  }

  def apply(tokenType: TokenType.Value, len: Int): Token =
    tokenType match {
      case TokenType.WHITESPACE =>
        new WhitespaceToken(len)
      case TokenType.COLON =>
        new ColonToken()
      case TokenType.NEWLINE =>
        new NewlineToken(len)
      case TokenType.DOT =>
        new SymbolToken(TokenType.DOT, ".")
      case TokenType.ELLIPSIS =>
        new SymbolToken(TokenType.ELLIPSIS, "...")
      case TokenType.CLASS_KEYWORD =>
        new NameToken(TokenType.CLASS_KEYWORD, "class")
      case TokenType.DEF_KEYWORD =>
        new NameToken(TokenType.DEF_KEYWORD, "def")
      case TokenType.LAMBDA =>
        new NameToken(TokenType.LAMBDA, "lambda")
      case _ =>
        new Token(tokenType, len)
    }

  def unapply(token: Token): Option[(TokenType.Value, Int)] =
    if (token != null)
      Some(token.tokenType, token.length)
    else
      None
}