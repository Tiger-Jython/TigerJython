/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.struct

import tigerjython.syntaxsupport.parser.StatementType
import tigerjython.syntaxsupport.tokens._

/**
 * @author Tobias Kohn
 */
class StructLine extends StructContainer {

  private var _stmtType: StatementType = _
  private var _indent: Int = 0
  private var _isEmpty: Boolean = true
  private var _scope: Scope = _

  def astNode: StatementType = _stmtType
  def astNode_=(node: StatementType): Unit = {
    val oldNode = _stmtType
    if (!(node eq oldNode)) {
      _stmtType = node
      astNodeChanged(oldNode, node)
    }
  }

  protected def astNodeChanged(oldNode: StatementType, newNode: StatementType): Unit =
    newNode match {
      case StatementType.FUNC_DEF(_) =>
        if (oldNode == null || !oldNode.isInstanceOf[StatementType.FUNC_DEF])
          scope = new Scope(program.getParentScopeForLine(this))
      case _ =>
        if (oldNode.isInstanceOf[StatementType.FUNC_DEF])
          scope = program.getParentScopeForLine(this)
    }

  private def checkIfEmpty(tokens: TokenArray): Boolean = {
    var i = this.index
    while (i < tokens.length)
      tokens(i).tokenType match {
        case TokenType.WHITESPACE | TokenType.COMMENT =>
          i += 1
        case TokenType.NEWLINE =>
          return true
        case _ =>
          return false
      }
    true
  }

  def countLinebreaks: Int =
    countLinebreaks(getDocument.tokens)

  def countLinebreaks(tokens: TokenArray): Int =
    countLinebreaks(tokens, index)

  def countLinebreaks(tokens: TokenArray, startIndex: Int): Int = {
    var result = 0
    for (i <- 0 until length;
         token = tokens(startIndex + i)
         if token != null)
      token.tokenType match {
        case TokenType.NEWLINE =>
          result += 1
        case _ =>
      }
    result
  }

  override def getScope: Scope =
    if (_scope != null)
      _scope
    else
      super.getScope

  def getTextLength: Int =
    getTextLength(getDocument.tokens)

  def getTextLength(tokens: TokenArray): Int =
    getTextLength(tokens, index)

  def getTextLength(tokens: TokenArray, startIndex: Int): Int = {
    var result = 0
    for (i <- 0 until length) {
      val tkn = tokens(startIndex + i)
      if (tkn != null)
        result += tkn.length
    }
    result
  }

  def hasIndent(indent: Int): Boolean =
    (_indent >= indent) || isEmpty

  def indent: Int = _indent
  def indent_=(i: Int): Unit =
    if (i != _indent && i >= 0) {
      val delta = i - _indent
      _indent = i
      program.notifyIndentationChanged(this, delta)
    }

  override protected[struct] def invalidate(tokens: TokenArray): Unit = {
    tokens(this.index) match {
      case w: WhitespaceToken =>
        _isEmpty = checkIfEmpty(tokens)
        indent = w.length
      case _: NewlineToken =>
        _isEmpty = true
        indent = 0
      case Token(tt, _) =>
        _isEmpty = (tt == TokenType.COMMENT)
        indent = 0
    }
    invalidateAstNode()
  }

  protected def invalidateAstNode(): Unit = {
    val parser = getParser
    if (parser != null)
      astNode = parser.parse(this)
  }

  def isEmpty: Boolean = _isEmpty

  override def listImportedModules(modules: collection.mutable.ArrayBuffer[String]): Unit =
    _stmtType match {
      case StatementType.IMPORT(mod) =>
        modules ++= mod
      case _ =>
    }

  override protected[struct] def offset: Int = 0
  override protected[struct] def offset_=(o: Int): Unit = {}

  override protected def parseHandleNewline(tokens: TokenArray, index: Int): Int = -1

  protected def program: StructProgram = parent.asInstanceOf[StructProgram]

  def scope: Scope = _scope
  def scope_=(s: Scope): Unit =
    if (!(scope eq this._scope)) {
      if (_scope != null)
        _scope.removeSite(this)
      _scope = s
      if (_scope != null)
        _scope.addSite(this)
    }

  protected[struct] def setParentScope(scope: Scope): Unit =
    if (astNode.isInstanceOf[StatementType.FUNC_DEF])
      this.scope.setParentScope(scope)
    else
      this.scope = scope
}
object StructLine {

  def apply(): StructLine = new StructLine()
}