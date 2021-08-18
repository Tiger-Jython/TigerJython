/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport

import tigerjython.syntaxsupport.struct._
import tigerjython.syntaxsupport.tokens._

/**
 * The `SyntaxDocument` represents the source code of a program, including information about its individual tokens and
 * its overall structure.  This is the interface and main entry point for getting syntax highlighting support.
 *
 * The document keeps a record of the actual text itself, together with an array of tokens, each with a type and a
 * length to quickly identify the individual elements in your source code.
 *
 * @author Tobias Kohn
 */
class SyntaxDocument {

  /* When we insert, delete or replace part of the text, we usually need to re-parse a larger portion of the text than
   * only what is covered directly by the affected tokens.  Consider, e.g., the case `foo + spam` where the middle part
   * ` + spa` is deleted.  At first sight, it seems the `foo` token is not directly affected by this operation, but
   * closer inspection reveals that it will be merger with the remainder of the `spam` token to form `foom`.
   */

  /* The `Document` keeps three separate 'lists' that represent the entire document/text:
   * - The `text` contains the actual text as a 'modifiable' string (actually `StringBuilder`)
   * - The `tokens` is an array with all the tokens representing the text
   * - The `struct` is a hierarchical representation of the code that identifies lines, suites, scopes, brackets, etc.
   *
   * It is crucial that all three representations are kept in sync!
   */
  protected val struct: StructProgram = createProgramTokenRange()
  protected[syntaxsupport] val text: StringBuilder = new StringBuilder()
  protected[syntaxsupport] val tokens: TokenArray = new TokenArray(this)

  protected val tokenizer: Tokenizer = createTokenizer()

  protected def createProgramTokenRange(): StructProgram =
    new StructProgram(this)

  protected def createTokenizer(): PythonTokenizer =
    new PythonTokenizer(this)

  def append(s: String): Unit =
    insert(text.length(), s)

  def clear(): Unit = synchronized {
    text.clear()
    tokens.clear()
    for (message <- tokens.createMessages())
      struct.handleMessage(message.index, message)
  }

  def delete(position: Int, delLength: Int): Unit = synchronized {
    if (delLength < 0) {
      val pos = (position + delLength) max 0
      delete(pos, position - pos)
    }
    else if (position + delLength > text.length)
      delete(position, text.length - position)
    else if (delLength > 0) {
      val (pos, index) = tokenIndexFromPosition(position)
      // Check for the special case where we are deleting whitespace, which requires no re-parsing
      tokens(index) match {
        case whitespaceToken: WhitespaceToken if position + delLength <= pos + whitespaceToken.length =>
          whitespaceToken.length -= delLength
          text.delete(position, position + delLength)
          return
        case Token(TokenType.NEWLINE, len) if position == pos + len && index + 1 < tokens.length =>
          tokens(index + 1) match {
            case whitespaceToken: WhitespaceToken =>
              if (whitespaceToken.length == delLength) {
                tokens.remove(index + 1)
                text.delete(position, position + delLength)
                return
              }
              else if (whitespaceToken.length > delLength) {
                tokens(index + 1).length -= delLength
                text.delete(position, position + delLength)
                return
              }
            case _ =>
          }
        case _ =>
      }
      val offset = position - pos
      var len: Int = tokens.remove(index).length
      var remainingDelLength = delLength - (len - offset)
      while (remainingDelLength > 0 && index < tokens.length) {
        val l = tokens.remove(index).length
        remainingDelLength -= l
        len += l
      }
      text.delete(position, position + delLength)
      invalidate(index, pos, pos + len - delLength)
    }
  }

  def getCurrentBlock(caretPos: Int): Option[(Int, Int)] = {
    val line = getLineIndexFromPosition(caretPos)
    if (0 <= line && line < struct.count) {
      val firstLineNo = struct.getFirstLineOfBlock(line)
      val lastLineNo = struct.getLastLineOfBlock(line)
      Some((firstLineNo, lastLineNo))
    }
    else if (line == struct.count) {
      val firstLineNo = struct.getFirstLineOfBlock(line - 1)
      Some((firstLineNo, line + 1))
    } else
      None
  }

  def getCurrentBlockRegion(caretPos: Int, fullScope: Boolean): Option[(Int, Int)] =
    if (fullScope)
      getCurrentScope(caretPos)
    else
      getCurrentBlock(caretPos)

  def getCurrentScope(caretPos: Int): Option[(Int, Int)] = {
    val line = getLineIndexFromPosition(caretPos)
    if (0 <= line && line < struct.count) {
      val firstLineNo = struct.getFirstLineOfScope(line)
      val lastLineNo = struct.getLastLineOfScope(line)
      Some((firstLineNo, lastLineNo))
    }
    else if (line == struct.count) {
      val firstLineNo = struct.getFirstLineOfScope(line - 1)
      Some((firstLineNo, line + 1))
    } else
      None
  }

  def getImportedModules: Array[String] = {
    val result = collection.mutable.ArrayBuffer[String]()
    struct.listImportedModules(result)
    result.toArray
  }

  protected def getLineIndexFromPosition(pos: Int): Int =
    if (pos <= text.length()) {
      var result = 0
      var i = 0
      while (i < pos) {
        if (text(i) == '\n')
          result += 1
        i += 1
      }
      result
    } else
      -1

  def insert(position: Int, insText: String): Unit = synchronized {
    if (insText != null && insText.nonEmpty) {
      if (position == 0) {
        val length =
          if (tokens.nonEmpty)
            tokens.remove(0).length
          else
            0
        text.insert(0, insText)
        invalidate(0, 0, length + insText.length)
      }
      else if (position < text.length) {
        if (insText.forall(_ == ' ') && text(position - 1).isWhitespace) {
          // Special case: inserting whitespace at a position where we already have whitespace, such as
          // increasing indentation (however, we only take the fast route for all-space strings, not tabulators)
          val (_, index) = tokenIndexFromPosition(position)
          tokens(index) match {
            case whitespaceToken: WhitespaceToken =>
              text.insert(position, insText)
              whitespaceToken.length += insText.length
              return
            case Token(TokenType.NEWLINE, _) =>
              text.insert(position, insText)
              if (index + 1 < tokens.length && tokens(index + 1).tokenType == TokenType.WHITESPACE)
                tokens(index + 1).length += insText.length
              else
                tokens.insert(index + 1, Token(TokenType.WHITESPACE, insText.length))
              return
            case _ =>
          }
        }
        val (pos, index) = tokenIndexFromPosition(position)
        val length =
          if (position == pos + tokens(index).length && index + 1 < tokens.length)
            // We are between two tokens ...
            tokens.remove(index + 1).length + tokens.remove(index).length
          else
            // We insert something into the middle of a token ...
            tokens.remove(index).length
        text.insert(position, insText)
        invalidate(index, pos, pos + length + insText.length)
      }
      else if (position == text.length) {
        val length =
          if (tokens.nonEmpty)
            tokens.remove(tokens.length - 1).length
          else
            0
        text.append(insText)
        invalidate(tokens.length, position - length, text.length)
      }
    }
  }

  protected def invalidate(index: Int, startPos: Int, endPos: Int): Unit =
    if (index > 0 && tokens(index - 1).tokenType == TokenType.NUMBER) {
      // A special case: if the preceding token is a number, it might be possible to connect to it
      // Example: 123e.45 -> '123', 'e', '.', '45';; remove the dot and it becomes one token '123e45'
      val len = tokens.remove(index - 1).length
      invalidate(index - 1, startPos - len, endPos)
    }
    else if (0 <= startPos && startPos < endPos && endPos <= text.length) {
      tokenizer.setParseRange(startPos, endPos)
      var len = endPos - startPos
      var i = index
      while (len > 0 && tokenizer.hasNext) {
        val token = tokenizer.next()
        tokens.insert(i, token)
        i += 1
        len -= token.length
        while (len < 0) {
          val l = tokens.remove(i).length
          len += l
          tokenizer.extendParseRange(l)
        }
      }
      for (message <- tokens.createMessages())
        struct.handleMessage(message.index, message)
    }

  def repeatIsKeyword: Boolean =
    tokenizer match {
      case p: PythonTokenizer =>
        p.repeatIsKeyword
      case _ =>
        false
    }
  def repeatIsKeyword_=(r: Boolean): Unit =
    tokenizer match {
      case p: PythonTokenizer =>
        p.repeatIsKeyword = r
      case _ =>
    }

  def replace(position: Int, delLength: Int, insText: String): Unit = synchronized {
    if (delLength < 0) {
      val pos = (position + delLength) max 0
      replace(pos, position - pos, insText)
    }
    else if (delLength == 0)
      insert(position, insText)
    else if (insText == null || insText.isEmpty)
      delete(position, delLength)
    else {
      val (pos, index) = tokenIndexFromPosition(position)
      val offset = position - pos
      var len: Int = tokens.remove(index).length
      var remainingDelLength = delLength - (len - offset)
      while (remainingDelLength > 0 && index < tokens.length) {
        val l = tokens.remove(index).length
        remainingDelLength -= l
        len += l
      }
      text.replace(position, position + delLength, insText)
      invalidate(index, pos, pos + len - delLength + insText.length)
    }
  }

  def setText(newText: String): Unit = synchronized {
    if (newText == null || newText.isEmpty)
      clear()
    else if (text.isEmpty)
      append(newText)
    else {
      // We check if we can keep large parts of the current text or whether we really have to replace the entire
      // text and re-parse it all.
      val oldLength = this.text.length
      val newLength = newText.length
      var i = 0
      val minLen = newLength min oldLength
      while (i < minLen && newText(i) == this.text(i))
        i += 1
      val prefixLen = i
      if (prefixLen == minLen) {
        // Special case: append new text or delete the end of the existing text
        if (prefixLen == this.text.length)
          append(newText.drop(prefixLen))
        else
          delete(prefixLen, this.text.length - prefixLen)
      } else {
        i = 1
        while (i <= minLen - prefixLen && newText(newLength - i) == this.text(oldLength - i))
          i += 1
        val suffixLen = i - 1
        val invariantLen = prefixLen + suffixLen
        // If more than half of the text is different, replace and re-parse it all
        if (invariantLen > 2 && invariantLen >= this.text.length / 2)
          replace(prefixLen, this.text.length - invariantLen,
            newText.substring(prefixLen, newLength - suffixLen))
        else {
          clear()
          append(newText)
        }
      }
    }
  }

  protected def tokenIndexFromPosition(position: Int): (Int, Int) = {
    var pos = 0
    var index = 0
    while (index < tokens.length && position > (pos + tokens(index).length)) {
      pos += tokens(index).length
      index += 1
    }
    (pos, index)
  }

  def visit(visitor: TokenVisitor): Unit = synchronized {
    for (token <- tokens)
      token.accept(this, visitor)
  }
}
