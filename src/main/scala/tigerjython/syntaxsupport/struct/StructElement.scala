/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.struct

import tigerjython.syntaxsupport.SyntaxDocument
import tigerjython.syntaxsupport.parser.StmtParser
import tigerjython.syntaxsupport.tokens.{Token, TokenArray}

/**
 * @author Tobias Kohn
 */
abstract class StructElement {

  private var _length: Int = 0
  private var _offset: Int = 0
  private var _parent: StructContainer = _

  def addDefName(name: String): Unit = {
    val scope = getScope
    if (scope != null)
      scope.addDef(name, this)
  }

  def addDelName(name: String): Unit = {
    val scope = getScope
    if (scope != null)
      scope.addDel(name, this)
  }

  def addLoadName(name: String): Unit = {
    val scope = getScope
    if (scope != null)
      scope.addLoad(name, this)
  }

  def addStoreName(name: String): Unit = {
    val scope = getScope
    if (scope != null)
      scope.addStore(name, this)
  }

  def delete(): Unit =
    if (_parent != null)
      _parent.deleteItem(this)

  def forAll(body: Token=>Unit): Unit = {
    val tokens = getDocument.tokens
    for (i <- index until (index + length))
      body(tokens(i))
  }

  protected def getDocument: SyntaxDocument =
    if (_parent != null)
      _parent.getDocument
    else
      null

  def getElement(relIndex: Int): StructElement =
    if (0 <= relIndex && relIndex <= length)
      this
    else
      null

  def getNestDepth: Int =
    if (_parent != null)
      _parent.getNestDepth
    else
      0

  def getParser: StmtParser =
    if (_parent != null)
      _parent.getParser
    else
      StmtParser.forDocument(getDocument)

  def getScope: Scope =
    if (_parent != null)
      _parent.getScope
    else
      null

  protected def _getToken(absIndex: Int): Token =
    if (_parent != null)
      _parent._getToken(absIndex)
    else
      null

  protected def getToken(relIndex: Int): Token =
    _getToken(this.index + relIndex)

  def handleMessage(relIndex: Int, message: TokenChangeMessage): Unit

  def index: Int =
    if (_parent != null)
      _parent.getIndexOf(this)
    else
      offset

  def length: Int = _length
  protected def length_=(l: Int): Unit =
    _length = l

  def listImportedModules(modules: collection.mutable.ArrayBuffer[String]): Unit

  protected[struct] def offset: Int = _offset
  protected[struct] def offset_=(o: Int): Unit =
    _offset = o

  protected[struct] def parent: StructContainer = _parent
  private[struct] def parent_=(p: StructContainer): Unit =
    _parent = p

  protected[struct] def parse(tokens: TokenArray, startIndex: Int): Int

  def removeName(name: String): Unit = {
    val scope = getScope
    if (scope != null)
      scope.removeNameFromSite(name, this)
  }

  protected[struct] def span: Int = offset + length
}
