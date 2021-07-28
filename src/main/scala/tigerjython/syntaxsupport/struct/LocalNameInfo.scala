/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.struct

/**
 * @author Tobias Kohn
 */
class LocalNameInfo(private val _name: String) extends NameInfo {

  private var _type: NameType.Value = NameType.FREE

  private val defSites = collection.mutable.Set[StructElement]()
  private val useSites = collection.mutable.Set[StructElement]()

  def addAll(other: NameInfo): Unit =
    other match {
      case nameInfo: LocalNameInfo =>
        this.defSites.addAll(nameInfo.defSites)
        this.useSites.addAll(nameInfo.useSites)
      case _ =>
    }

  def addDef(site: StructElement): Unit =
    if (site != null) {
      defSites += site
      if (_type == NameType.FREE)
        _type = NameType.LOCAL
    }

  def addDel(site: StructElement): Unit =
    if (site != null) {
      defSites += site
      if (_type == NameType.FREE)
        _type = NameType.LOCAL
    }

  def addLoad(site: StructElement): Unit =
    if (site != null)
      useSites += site

  def addStore(site: StructElement): Unit =
    if (site != null) {
      defSites += site
      if (_type == NameType.FREE)
        _type = NameType.LOCAL
    }

  def isEmpty: Boolean = defSites.isEmpty && useSites.isEmpty

  def name: String = _name

  def nameType: NameType.Value = _type
  def nameType_=(nt: NameType.Value): Unit =
    _type = nt

  def removeSite(site: StructElement): Unit = {
    defSites -= site
    useSites -= site
  }
}
