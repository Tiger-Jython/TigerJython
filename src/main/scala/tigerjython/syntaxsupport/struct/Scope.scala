/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.struct

import tigerjython.syntaxsupport.tokens.{NameToken, TokenType}

/**
 * @author Tobias Kohn
 */
class Scope(var parentScope: Scope) {

  private val _names = collection.mutable.Map[String, NameInfo]()

  protected def getNameInfo(name: String): NameInfo =
    _names.get(name) match {
      case Some(nameInfo) =>
        nameInfo
      case None =>
        val result =
          if (parentScope != null)
            parentScope.getNameInfo(name)
          else
            null
        if (result == null) {
          val r = new LocalNameInfo(name)
          _names(name) = r
          r
        } else
          result
    }

  def addDef(name: String, site: StructElement): NameInfo =
    if (name != null && name != "" && site != null) {
      val nameInfo = getNameInfo(name)
      nameInfo.addDef(site)
      nameInfo
    } else
      null

  def addDel(name: String, site: StructElement): NameInfo =
    if (name != null && name != "" && site != null) {
      val nameInfo = getNameInfo(name)
      nameInfo.addDel(site)
      nameInfo
    } else
      null

  def addLoad(name: String, site: StructElement): NameInfo =
    if (name != null && name != "" && site != null) {
      val nameInfo = getNameInfo(name)
      nameInfo.addDel(site)
      nameInfo
    } else
      null

  def addParameter(name: String, site: StructElement): NameInfo =
    if (name != null && name != "" && site != null) {
      val nameInfo = getNameInfo(name)
      nameInfo.nameType = NameType.PARAMETER
      nameInfo.addStore(site)
      nameInfo
    } else
      null

  def addSite(site: StructElement): Unit = {
    site.forAll({
      case n @ NameToken(TokenType.NAME | TokenType.BUILTIN_NAME, nameType, s) =>
      case _ =>
    })
  }

  def addStore(name: String, site: StructElement): NameInfo =
    if (name != null && name != "" && site != null) {
      val nameInfo = getNameInfo(name)
      nameInfo.addStore(site)
      nameInfo
    } else
      null

  def removeNameFromSite(name: String, site: StructElement): Unit =
    _names.get(name) match {
      case Some(nameInfo) =>
        nameInfo.removeSite(site)
        if (nameInfo.isEmpty)
          _names.remove(name)
      case None =>
    }

  def removeSite(site: StructElement): Unit = {
    val emptySets = collection.mutable.Set[String]()
    for (nameInfo <- _names.values) {
      nameInfo.removeSite(site)
      if (nameInfo.isEmpty)
        emptySets += nameInfo.name
    }
    for (name <- emptySets)
      _names.remove(name)
  }

  def makeGlobal(name: String, site: StructElement): Unit =
    if (name != null && name != "")
      _names.get(name) match {
        case Some(nameInfo: LocalNameInfo) if nameInfo.nameType != NameType.PARAMETER =>
          if (parentScope != null) {
            val globalNameInfo = parentScope.getNameInfo(name)
            globalNameInfo.addAll(nameInfo)
            _names(name) = new NonlocalNameInfo(globalNameInfo)
          } else
            if (nameInfo.nameType == NameType.FREE)
              nameInfo.nameType = NameType.GLOBAL
        case Some(_: NonlocalNameInfo) =>
          // Nothing to do
        case None =>
          if (parentScope != null) {
            val nameInfo = parentScope.getNameInfo(name)
            _names(name) = new NonlocalNameInfo(nameInfo)
          }
      }

  def makeNonlocal(name: String, site: StructElement): Unit =
    makeGlobal(name, site)

  def setParentScope(scope: Scope): Unit =
    if (scope != null && !(scope eq this))
      parentScope = scope
}
