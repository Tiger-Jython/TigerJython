/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.tokens

import tigerjython.syntaxsupport.SyntaxDocument
import tigerjython.syntaxsupport.struct.TokenChangeMessage

import scala.collection.mutable.ArrayBuffer

/**
 * This is a wrapper around an ArrayBuffer of tokens that will record any changes to tokens, such as deletion
 * or insertion of tokens.  These changes can then be aggregated into a message about these changes to be
 * broadcast.  The mechanism is very close to what is typically found for undo-managers, although with a slightly
 * different aim, of course.
 *
 * @author Tobias Kohn
 */
class TokenArray(val document: SyntaxDocument) extends Iterable[Token] {

  import TokenArray._

  private val changes: ArrayBuffer[TokenArrayChange] = new ArrayBuffer[TokenArrayChange]()
  private val items: ArrayBuffer[Token] = new ArrayBuffer[Token]()

  private var _bracketBalance: Int = 0
  private var _linesCount: Int = 0

  private def recordChange(change: TokenArrayChange): Unit = {
    if (changes.nonEmpty)
      changes.last.mergeWith(change) match {
        case Some(null) =>
          changes.remove(changes.length - 1)
          return
        case Some(altChange) =>
          changes(changes.length - 1) = altChange
          return
        case None =>
      }
    changes.append(change)
  }

  def append(item: Token): Unit =
    if (item != null) {
      if (items.isEmpty)
        _linesCount = 1
      items.append(item)
      itemInserted(item.tokenType)
      recordChange(TokenArrayInsertion(items.length - 1, 1))
    }

  def apply(index: Int): Token = items(index)

  def bracketBalance: Int = _bracketBalance

  def clear(): Unit = {
    recordChange(TokenArrayDeletion(0, items.toArray))
    items.clear()
    _bracketBalance = 0
    _linesCount = 0
  }

  def createMessage(): TokenChangeMessage =
    if (changes.nonEmpty) {
      changes.remove(0) match {
        case TokenArrayDeletion(index, delTokens) =>
          TokenChangeMessage.TokensDeleted(this, index, delTokens.length)
        case TokenArrayInsertion(index, insTokens) =>
          TokenChangeMessage.TokensInserted(this, index, insTokens)
        case TokenArrayReplacement(index, _) =>
          TokenChangeMessage.TokenChanged(this, index)
      }
    } else
      null

  def createMessages(): Array[TokenChangeMessage] = {
    val result = new ArrayBuffer[TokenChangeMessage]()
    while (changes.nonEmpty) {
      val message = createMessage()
      if (message != null)
        result += message
    }
    result.toArray
  }

  /**
   * Returns a list with all 'semantically active' tokens of the current line.  That is, whitespace is removed, except
   * for the leading whitespace possibly indicating an indentation.
   *
   * The index is the position of a token on the line, not the line number.
   *
   * The result is a list, not an array, so as to support easy pattern matching on it.
   */
  def getTokensOfLine(index: Int): List[Token] = {
    var i = index
    while (i > 0 && items(i - 1).tokenType != TokenType.NEWLINE)
      i -= 1
    val result = new collection.mutable.ListBuffer[Token]()
    // We add only the very first whitespace and filter out the rest
    if (i < items.length && items(i).tokenType == TokenType.WHITESPACE)
      result += items(i)
    while (i < items.length && items(i).tokenType != TokenType.NEWLINE) {
      val item = items(i)
      if (item.tokenType != TokenType.WHITESPACE && item.tokenType != TokenType.COMMENT)
        result += item
    }
    result.toList
  }

  def indexOf(item: Token): Int = items.indexOf(item)

  def insert(index: Int, item: Token): Unit =
    if (item != null) {
      items.insert(index, item)
      itemInserted(item.tokenType)
      recordChange(TokenArrayInsertion(index, 1))
    }

  override def isEmpty: Boolean = items.isEmpty

  def isLineEmpty(index: Int): Boolean =
    if (0 <= index && index < items.length)
      items(index).tokenType match {
        case TokenType.WHITESPACE =>
          isStmtOnly(index + 1)
        case TokenType.NEWLINE | TokenType.COMMENT =>
          true
        case _ =>
          false
      }
    else
      false

  def isStmtOnly(index: Int): Boolean =
    if (0 <= index && index < items.length)
      items(index) match {
        case _: WhitespaceToken =>
          isStmtOnly(index + 1)
        case nameToken: NameToken =>
          nameToken.isStmtOnlyKeyword
        case _ =>
          false
      }
    else
      false

  def isWhitespace(index: Int): Boolean =
    if (0 <= index && index < items.length)
      items(index).tokenType == TokenType.WHITESPACE
    else
      false

  private def itemInserted(tokenType: TokenType.Value): Unit =
    tokenType match {
      case TokenType.LEFT_BRACKET =>
        _bracketBalance += 1
      case TokenType.RIGHT_BRACKET =>
        _bracketBalance -= 1
      case TokenType.NEWLINE =>
        _linesCount += 1
      case _ =>
    }

  private def itemRemoved(tokenType: TokenType.Value): Unit =
    tokenType match {
      case TokenType.LEFT_BRACKET =>
        _bracketBalance -= 1
      case TokenType.RIGHT_BRACKET =>
        _bracketBalance += 1
      case TokenType.NEWLINE =>
        _linesCount -= 1
      case _ =>
    }

  def iterator: Iterator[Token] = items.iterator

  def length: Int = items.length

  def linesCount: Int = _linesCount

  override def nonEmpty: Boolean = items.nonEmpty

  def remove(index: Int): Token = {
    val result = items.remove(index)
    recordChange(TokenArrayDeletion(index, Array(result)))
    itemRemoved(result.tokenType)
    result
  }

  def update(index: Int, newItem: Token): Unit =
    if (newItem != null) {
      val oldItem = items(index)
      itemRemoved(oldItem.tokenType)
      items(index) = newItem
      itemInserted(newItem.tokenType)
      recordChange(TokenArrayReplacement(index, oldItem))
    } else
      remove(index)
}

object TokenArray {

  private sealed abstract class TokenArrayChange {

    def mergeWith(newChange: TokenArrayChange): Option[TokenArrayChange]
  }

  private case class TokenArrayDeletion(index: Int, items: Array[Token]) extends TokenArrayChange {

    def mergeWith(newChange: TokenArrayChange): Option[TokenArrayChange] =
      newChange match {
        case TokenArrayDeletion(otherIndex, otherItems) =>
          if (index == otherIndex)
            Some(TokenArrayDeletion(index, items ++ otherItems))
          else
            None
        case _ =>
          None
      }

    override def toString: String = f"TokenArrayDeletion($index,[${items.mkString(",")}])"
  }

  private case class TokenArrayInsertion(index: Int, count: Int) extends TokenArrayChange {

    def mergeWith(newChange: TokenArrayChange): Option[TokenArrayChange] =
      newChange match {
        case TokenArrayDeletion(otherIndex, otherItems) if otherIndex == index =>
          if (otherItems.length == count)
            Some(null)
          else if (otherItems.length < count)
            Some(TokenArrayInsertion(index, count - otherItems.length))
          else
            None
        case TokenArrayInsertion(otherIndex, otherCount) =>
          if (index + count == otherIndex)
            Some(TokenArrayInsertion(index, count + otherCount))
          else if (otherIndex + otherCount == index)
            Some(TokenArrayInsertion(otherIndex, otherCount + count))
          else
            None
        case _ =>
          None
      }
  }

  private case class TokenArrayReplacement(index: Int, oldItem: Token) extends TokenArrayChange {

    def mergeWith(newChange: TokenArrayChange): Option[TokenArrayChange] =
      newChange match {
        case TokenArrayDeletion(otherIndex, otherItems) if otherIndex == index && otherItems.length == 1 =>
          Some(TokenArrayDeletion(index, Array(oldItem)))
        case _ =>
          None
      }
  }
}