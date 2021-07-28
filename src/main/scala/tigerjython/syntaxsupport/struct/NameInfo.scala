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
abstract class NameInfo {

  def addAll(other: NameInfo): Unit

  def addDef(site: StructElement): Unit

  def addDel(site: StructElement): Unit

  def addLoad(site: StructElement): Unit

  def addStore(site: StructElement): Unit

  def isEmpty: Boolean

  def name: String

  var nameType: NameType.Value

  def nonEmpty: Boolean = !isEmpty

  def removeSite(site: StructElement): Unit

  def uniqueIdentity: Int = hashCode()
}
