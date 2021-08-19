/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.struct

import tigerjython.syntaxsupport.SyntaxDocument
import tigerjython.syntaxsupport.parser.{PythonStmtParser, StmtParser}
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
            val gapLen = document.tokens.length - childrenSpan
            _gapEnd = _gapStart + gapLen
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

  def apply(i: Int): StructLine = children(i).asInstanceOf[StructLine]

  protected def createGlobalScope(): Scope = new Scope(null)

  protected def createParser(): StmtParser =
    new PythonStmtParser(document)

  override protected def getDocument: SyntaxDocument = document

  def getFirstLineOfBlock(lineNo: Int): Int = {
    var i = lineNo
    while (i > 0 && this(i).isEmpty)
      i -= 1
    val indent = this(i).indent
    if (i == lineNo && i+1 < children.length && this(i+1).indent > indent)
      return lineNo
    if (indent == 0)
      return 0
    while (i > 0 && this(i-1).hasIndent(indent))
      i -= 1
    (i - 1) max 0
  }

  def getLastLineOfBlock(lineNo: Int): Int = {
    var i = lineNo
    while (i > 0 && this(i).isEmpty)
      i -= 1
    val indent = this(i).indent
    if (indent == 0)
      children.length + 1
    i = lineNo
    while (i < children.length && this(i).hasIndent(indent))
      i += 1
    i
  }

  def getBlockExtent(lineNo: Int): (Int, Int) = {
    var i = lineNo
    while (i > 0 && this(i).isEmpty)
      i -= 1
    val indent = this(i).indent
    // Check for the special case where we are on the 'head' line of a composite statement
    if (i == lineNo && i+1 < children.length && this(i+1).indent > indent) {
      i = lineNo + 1
      val indent = this(i+1).indent
      while (i < children.length && this(i).hasIndent(indent))
        i += 1
      (getPhysicalLineOf(lineNo), getPhysicalLineOf(i))
    }
    else if (indent > 0) {
      while (i > 0 && this(i-1).hasIndent(indent))
        i -= 1
      val startLine = (i - 1) max 0
      i = lineNo
      while (i < children.length && this(i).hasIndent(indent))
        i += 1
      (getPhysicalLineOf(startLine), getPhysicalLineOf(i))
    } else
      (-1, -1)
      //(0, getPhysicalLineOf(children.length))
  }

  def getFirstLineOfScope(lineNo: Int): Int = {
    var i = lineNo
    while (i > 0 && this(i).isEmpty)
      i -= 1
    val indent = this(i).indent
    if (indent == 0)
      return 0
    while (i > 0 && this(i-1).hasIndent(indent))
      i -= 1
    (i - 1) max 0
  }

  def getLastLineOfScope(lineNo: Int): Int = {
    var i = lineNo
    while (i > 0 && this(i).isEmpty)
      i -= 1
    val indent = this(i).indent
    if (indent == 0)
      children.length + 1
    i = lineNo
    while (i < children.length && this(i).hasIndent(indent))
      i += 1
    i
  }

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

  def getPhysicalLineOf(line: Int): Int = {
    val tokens = getDocument.tokens
    var result = 0
    var idx = 0
    for (i <- 0 until line;
         ln = apply(i)) {
      result += ln.countLinebreaks(tokens, idx)
      idx += ln.length
    }
    result
  }

  def getPhysicalLineOf(line: StructLine): Int = {
    val tokens = getDocument.tokens
    var result = 0
    var idx = 0
    for (l <- children;
         ln = l.asInstanceOf[StructLine])
      if (ln eq line)
        return result
      else {
        result += ln.countLinebreaks(tokens, idx)
        idx += ln.length
      }
    result
  }

  override def getScope: Scope = scope

  override protected def _getToken(absIndex: Int): Token =
    document.tokens(absIndex)

  def handleMessage(message: TokenChangeMessage): Unit =
    handleMessage(message.index, message)

  override def handleMessage(relIndex: Int, message: TokenChangeMessage): Unit = {
    passMessageToChildren(message.index, message)
    if (!message.isHandled) {
      if (children.isEmpty) {
        _gapIndex = 0
        _gapStart = 0
        _gapEnd = message.tokens.length
      } else
        message match {
          case TokenChangeMessage.TokenChanged(_, index) =>
            setGap(index, index)
          case TokenChangeMessage.TokensDeleted(_, index, delCount) =>
            setGap(index, index + delCount)
          case TokenChangeMessage.TokensInserted(_, index, insCount) =>
            setGap(index, index)
        }
      if (_gapStart < _gapEnd)
        parse(message.tokens, _gapStart)
    }
  }

  def notifyIndentationChanged(sender: StructLine, delta: Int): Unit = {
    var i = children.indexOf(sender)
    sender.setParentScope(getParentScopeForLine(i))
    i += 1
    val baseIndent = if (delta < 0) sender.indent else sender.index - delta
    while (i < children.length && (this(i).indent > baseIndent || this(i).isEmpty)) {
      val newScope = getParentScopeForLine(i)
      this(i).setParentScope(newScope)
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
