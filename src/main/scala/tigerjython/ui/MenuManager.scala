/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui

import javafx.scene.Node
import javafx.scene.control.MenuBar

/**
 * To allow customised menus, we define an interface for a menu-manager, which is responsible for creating menus and
 * updating its entries in case new elements receive the focus.
 *
 * @author Tobias Kohn
 */
trait MenuManager {

  /**
   * This method is called to inform the menu manager about a change of focus so that it can update menu entries
   * where appropriate (enabling/disabling clipboard actins, for instance).
   */
  def focusChanged(node: Node): Unit

  /**
   * Creates the main menu to be displayed at the top of the window (or system).
   */
  def createMenu: MenuBar

  val mainMenu: MenuBar = createMenu
}
