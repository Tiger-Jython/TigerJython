/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.struct

import tigerjython.syntaxsupport.SyntaxDocument
import tigerjython.syntaxsupport.parser.{PythonStmtParser, StmtParser, TokenSource}
import tigerjython.syntaxsupport.tokens._

/**
 * @author Tobias Kohn
 */
class StructProgram(val document: SyntaxDocument) extends StructContainer {

  private var _gapEnd: Int = 0
  private var _gapIndex: Int = 0
  private var _gapStart: Int = 0

  protected var parser: StmtParser = createParser()
  protected val scope: Scope = createGlobalScope()

  private def setGap(startIndex: Int, endIndex: Int): Unit = {
    var i = 0
    var offs: Int = 0
    while (i < children.length) {
      val child = children(i)
      offs += child.length
      if (startIndex < offs) {
        _gapStart = offs - child.length
        _gapIndex = i
        while (i < children.length) {
          val child = children.remove(i)
          if (endIndex < offs) {
            _gapEnd = offs
            return
          } else
            offs += child.length
        }
      } else
        i += 1
    }
    _gapEnd = document.tokens.length
  }

  override def append(item: StructElement): StructElement =
    if (item != null && _gapStart < _gapEnd) {
      children.insert(_gapIndex, item)
      item.parent = this
      _gapIndex += 1
      item
    } else
      super.append(item)

  protected def createGlobalScope(): Scope = new Scope(null)

  protected def createParser(): StmtParser =
    new PythonStmtParser(document)

  override protected def getDocument: SyntaxDocument = document

  def getParentScopeForLine(line: Int): Scope = {
    val indent = children(line).asInstanceOf[StructLine].indent
    if (indent == 0)
      return scope
    var i = line - 1
    while (i >= 0) {
      val curLine = children(i).asInstanceOf[StructLine]
      if (curLine.indent < indent && !curLine.isEmpty)
        return curLine.getScope
      i -= 1
    }
    scope
  }

  def getParentScopeForLine(line: StructLine): Scope =
    getParentScopeForLine(children.indexOf(line))

  override def getScope: Scope = scope

  override protected def _getToken(absIndex: Int): Token =
    document.tokens(absIndex)

  def handleMessage(message: TokenChangeMessage): Unit =
    handleMessage(message.index, message)

  override def handleMessage(relIndex: Int, message: TokenChangeMessage): Unit = {
    passMessageToChildren(relIndex, message)
    if (!message.isHandled) {
      if (children.isEmpty) {
        _gapIndex = 0
        _gapStart = 0
        _gapEnd = message.tokens.length
      } else
        message match {
          case TokenChangeMessage.TokenChanged(_, index) =>
            setGap(index, index)
          case TokenChangeMessage.TokensDeleted(_, index, _) =>
            setGap(index, index)
          case TokenChangeMessage.TokensInserted(_, index, insCount) =>
            setGap(index, index + insCount)
        }
      if (_gapStart < _gapEnd)
        parse(message.tokens, _gapStart)
    }
  }

  def line(i: Int): StructLine = children(i).asInstanceOf[StructLine]

  def notifyIndentationChanged(sender: StructLine, delta: Int): Unit = {
    var i = children.indexOf(sender)
    sender.setParentScope(getParentScopeForLine(i))
    i += 1
    val baseIndent = if (delta < 0) sender.indent else sender.index - delta
    while (i < children.length && (line(i).indent > baseIndent || line(i).isEmpty)) {
      val newScope = getParentScopeForLine(i)
      line(i).setParentScope(newScope)
      i += 1
    }
  }

  override protected[struct] def parse(tokens: TokenArray, startIndex: Int): Int = {
    var i = startIndex
    while (i < tokens.length && i < _gapEnd) {
      val line = append(StructLine()).asInstanceOf[StructLine]
      i += line.parse(tokens, i)
      line.invalidate(tokens)
      _gapStart += line.length
      while (_gapStart > _gapEnd && children.length > _gapIndex)
        _gapEnd += children.remove(_gapIndex).length
    }
    length = document.tokens.length
    i - startIndex
  }
}
