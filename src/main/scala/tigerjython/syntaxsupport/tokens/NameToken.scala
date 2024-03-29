/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.tokens

import tigerjython.syntaxsupport.struct.NameInfo

/**
 * @author Tobias Kohn
 */
class NameToken(tt: TokenType.Value, private var _text: String) extends Token(tt, _text.length) {

  private var _isStmtOnlyKeyword: Boolean = false

  private var _nameTokenType: NameTokenType.Value = NameTokenType.UNKNOWN

  var nameInfo: NameInfo = _
  // var nameTokenType: NameTokenType.Value = NameTokenType.UNKNOWN

  def nameTokenType: NameTokenType.Value = _nameTokenType
  def nameTokenType_=(ntt: NameTokenType.Value): Unit = {
    _nameTokenType = ntt
  }

  override protected def getStyleName: String =
    tokenType match {
      case TokenType.KEYWORD | TokenType.LAMBDA =>
        "keyword"
      case TokenType.DEF_KEYWORD | TokenType.CLASS_KEYWORD =>
        "def-keyword"
      case TokenType.BUILTIN_NAME =>
        "builtin"
      case _ =>
        nameTokenType match {
          case NameTokenType.MODULE =>
            "module-name"
          case NameTokenType.DEF_NAME =>
            "def-name"
          case NameTokenType.CALL =>
            "name-call"
          case NameTokenType.FORMAL_ARG =>
            "name-arg"
          case NameTokenType.ARG_KEYWORD =>
            "name-arg-keyword"
          case NameTokenType.STORE | NameTokenType.ASSIGN_TARGET | NameTokenType.DEL_TARGET =>
            "name-store"
          case _ =>
            "name"
        }
    }

  override def equals(obj: Any): Boolean =
    obj match {
      case nameToken: NameToken =>
        tokenType == nameToken.tokenType && text == nameToken.text
      case s: String =>
        s == text
      case _ =>
        super.equals(obj)
    }

  def isKeyword: Boolean =
    tokenType == TokenType.KEYWORD || tokenType == TokenType.DEF_KEYWORD ||
      tokenType == TokenType.LAMBDA || tokenType == TokenType.CLASS_KEYWORD

  def isName: Boolean =
    tokenType == TokenType.NAME || tokenType == TokenType.BUILTIN_NAME

  def isStmtOnlyKeyword: Boolean = _isStmtOnlyKeyword

  private[syntaxsupport] def setStmtOnlyKeyword(r: Boolean): Unit =
    _isStmtOnlyKeyword = r

  def text: String = _text

  def text_=(s: String): Unit = {
    _text = s
    length = s.length
  }

  override def toString: String =
    if (nameInfo != null && tokenType == TokenType.NAME)
      f"Name(`$text`,$nameTokenType,${nameInfo.uniqueIdentity})"
    else if (tokenType == TokenType.NAME)
      f"Name(`$text`,$nameTokenType)"
    else
      f"${tokenType.toString}(`$text`)"
}

object NameToken {

  def unapply(nameToken: NameToken): Option[(TokenType.Value, NameTokenType.Value, String)] =
    if (nameToken != null)
      Some(nameToken.tokenType, nameToken.nameTokenType, nameToken.text)
    else
      None

  object Keyword {

    def unapply(nameToken: NameToken): Option[String] =
      if (nameToken != null)
        nameToken.tokenType match {
          case TokenType.KEYWORD =>
            Some(nameToken.text)
          case TokenType.CLASS_KEYWORD =>
            Some("class")
          case TokenType.DEF_KEYWORD =>
            Some("def")
          case TokenType.LAMBDA =>
            Some("lambda")
          case _ =>
            None
        }
      else
        None
  }
}
