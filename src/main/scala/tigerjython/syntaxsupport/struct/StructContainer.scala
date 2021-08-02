/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.struct

import tigerjython.syntaxsupport.tokens._

import scala.collection.mutable.ArrayBuffer

/**
 * @author Tobias Kohn
 */
abstract class StructContainer extends StructElement {

  protected val children: ArrayBuffer[StructElement] = new ArrayBuffer[StructElement]()

  private def notifyLengthAdjusted(sender: StructElement, delta: Int): Unit =
    if (!(sender eq children.last)) {
      val idx = children.indexOf(sender)
      if (delta < children(idx + 1).offset)
        children(idx + 1).offset -= delta
      else
        children(idx + 1).offset = 0
    } else
      adjustLength()

  protected def adjustLength(): Int = {
    val len = childrenSpan
    if (len > length) {
      val delta = len - length
      length = len
      if (parent != null)
        parent.notifyLengthAdjusted(this, delta)
    }
    length
  }

  def append(item: StructElement): StructElement =
    if (item != null) {
      children += item
      item.parent = this
      adjustLength()
      item
    } else
      null

  protected def childrenSpan: Int =
    children.map(_.span).sum

  def clear(): Unit = {
    for (child <- children)
      child.parent = null
    children.clear()
  }

  protected def clearFromIndex(index: Int): Unit = {
    for (i <- index until children.length)
      children(i).parent = null
    children.remove(index, children.length - index)
  }

  protected def clearFromIndex(item: StructElement): Unit =
    clearFromIndex(children.indexOf(item))

  protected def clearRange(fromIndex: Int, untilIndex: Int): Unit = {
    for (i <- fromIndex until untilIndex)
      children(i).parent = null
    children.remove(fromIndex, untilIndex - fromIndex)
  }

  protected def clearRange(fromItem: StructElement, untilItem: StructElement): Unit =
    clearRange(children.indexOf(fromItem), children.indexOf(untilItem))

  private[struct] def deleteItem(item: StructElement): Unit = {
    val idx = children.indexOf(item)
    children.remove(idx)
    item.parent = null
    if (idx < children.length)
      children(idx).offset += item.span
  }

  private def getChildForIndex(relIndex: Int): Option[(Int, StructElement)] = {
    var idx = relIndex
    for (item <- children) {
      idx -= item.offset
      if (idx <= item.length)
        return Some((idx, item))
      idx -= item.length
    }
    None
  }

  override def getElement(relIndex: Int): StructElement =
    if (0 <= relIndex && relIndex <= length) {
      var idx = relIndex
      for (item <- children) {
        idx -= item.offset
        val result = item.getElement(idx)
        if (result != null)
          return result
        idx -= item.length
      }
      this
    } else
      null

  private[struct] def getIndexOf(item: StructElement): Int = {
    var result = this.index
    for (child <- children)
      if (child eq item)
        return result + item.offset
      else
        result += child.span
    -1
  }

  def handleMessage(relIndex: Int, message: TokenChangeMessage): Unit = {
    passMessageToChildren(relIndex, message)
    if (!message.isHandled && 0 <= relIndex && relIndex <= length)
      message match {
        case TokenChangeMessage.TokenChanged(tokens, index) =>
          // TODO: handle this case
          invalidate(tokens)
        case TokenChangeMessage.TokensDeleted(tokens, index, delCount) if relIndex + delCount < length =>
          if (handleDeleteMessage(relIndex, tokens, index, delCount))
            message.handled()
          invalidate(tokens)
        case insMsg @ TokenChangeMessage.TokensInserted(tokens, _, insCount) if relIndex < length =>
          if (!insMsg.hasBrackets && (!insMsg.hasNewlines || getNestDepth > 0)) {
            getChildForIndex(relIndex) match {
              case Some((offs, item)) if offs < 0 =>
                item.offset += insCount
                length += insCount
                message.handled()
              case None =>
                length += insCount
                message.handled()
            }
          }
          invalidate(tokens)
        case _ =>
      }
  }

  protected def handleDeleteMessage(relIndex: Int, tokens: TokenArray, index: Int, delCount: Int): Boolean =
    if (children.nonEmpty)
      // At this point we already know that none of the children can handle this message on its own
      // Perhaps we get away without having to parse anything or at least, we can minimise the amount we need to parse
      getChildForIndex(relIndex) match {
        case Some((startOffs, startItem)) =>
          if (startOffs < 0) {
            getChildForIndex(relIndex + delCount) match {
              case Some((endOffs, endItem)) if endOffs < 0 =>
                if (startItem eq endItem) {
                  startItem.offset -= delCount
                  length -= delCount
                } else {
                  endItem.offset = startItem.offset + startOffs - endOffs
                  clearRange(startItem, endItem)
                  length -= delCount
                }
              case None =>
                clearFromIndex(startItem)
                length -= delCount
              case _ =>
                parse(tokens, index)
            }
          } else
            parse(tokens, index)
          true
        case _ =>
          length -= delCount
          true
      }
    else {
      length -= delCount
      if (length == 0)
        delete()
      true
    }

  protected[struct] def invalidate(tokens: TokenArray): Unit = {}

  def listImportedModules(modules: collection.mutable.ArrayBuffer[String]): Unit =
    for (child <- children)
      child.listImportedModules(modules)

  protected[struct] def parse(tokens: TokenArray, startIndex: Int): Int = {
    val relStartIndex = startIndex - this.index
    var i = startIndex
    if (children.nonEmpty) {
      var offs = relStartIndex
      var j = 0
      while (j < children.length) {
        val child = children(j)
        offs -= child.offset
        if (offs < child.length) {
          clearFromIndex(j)
          if (offs > 0)
            i -= offs
        } else
          offs -= child.length
        j += 1
      }
    }
    ///////////////////////////////////////////////////////
    val isNested = getNestDepth > 0
    while (i < tokens.length) {
      val j =
        tokens(i) match {
          case tkn: LeftBracketToken =>
            val item = append(StructBracketExpr((i - this.index) - childrenSpan))
            tkn.structElement = item
            1 + item.parse(tokens, i + 1)
          case tkn: RightBracketToken =>
            tkn.structElement = this
            parseHandleRightBracket(tokens, i)
          case tkn: NewlineToken =>
            tkn.nested = isNested
            tkn.structElement = this
            parseHandleNewline(tokens, i)
          case tkn =>
            tkn.nested = isNested
            tkn.structElement = this
            1
        }
      if (j <= 0) {
        i -= j    // This is really i += abs(j)
        length = i - this.index
        return i - startIndex
      } else
        i += j
    }
    length = i - this.index
    adjustLength - relStartIndex
  }

  protected def parseHandleRightBracket(tokens: TokenArray, index: Int): Int = 1

  protected def parseHandleNewline(tokens: TokenArray, index: Int): Int = 0

  protected def passMessageToChildren(relIndex: Int, message: TokenChangeMessage): Unit = {
    var i = 0
    var relIdx = relIndex
    while (!message.isHandled && i < children.length) {
      val child = children(i)
      relIdx -= child.offset
      child.handleMessage(relIdx, message)
      relIdx -= child.length
      i += 1
    }
  }
}
