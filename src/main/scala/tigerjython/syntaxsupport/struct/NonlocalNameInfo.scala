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
class NonlocalNameInfo(val target: NameInfo) extends NameInfo {

  def addAll(other: NameInfo): Unit =
    target.addAll(other)

  def addDef(site: StructElement): Unit =
    target.addDef(site)

  def addDel(site: StructElement): Unit =
    target.addDel(site)

  def addLoad(site: StructElement): Unit =
    target.addLoad(site)

  def addStore(site: StructElement): Unit =
    target.addStore(site)

  def isEmpty: Boolean = target.isEmpty

  def name: String = target.name

  def nameType: NameType.Value = NameType.NONLOCAL
  def nameType_=(nt: NameType.Value): Unit = {}

  override def nonEmpty: Boolean = target.nonEmpty

  def removeSite(site: StructElement): Unit =
    target.removeSite(site)
}
