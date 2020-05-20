/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui

import javafx.scene.Node
import javafx.scene.control.{Menu, MenuBar, MenuItem}

/**
 * To allow customised menus, we define an interface for a menu-manager, which is responsible for creating menus and
 * updating its entries in case new elements receive the focus.
 *
 * @author Tobias Kohn
 */
trait MenuManager {

  private def _getMenu(name: String): Menu = {
    val n =
      if (name != null && name != "")
        name.toLowerCase + "-menu"
      else
        "tools-menu"
    mainMenu.getMenus.forEach({ menu =>
      if (menu.getId == n)
        return menu
    })
    val toolsMenu = createToolsMenu
    Utils.onFX(() => {
      mainMenu.getMenus.add(toolsMenu)
    })
    toolsMenu
  }

  /**
   * Adds a new menu entry.
   *
   * @param name     A name that identifies the menu.  It can be prefixed by the name of another menu.
   * @param caption  The caption/text to show for the given menu.
   * @param action   A runnable that is executed when the menu item is selected.
   */
  def addMenuItem(name: String, caption: String, action: Runnable): Unit =
    if (mainMenu != null) {
      val names = name.split('.')
      val menu =
        if (names.length == 1)
          _getMenu(null)
        else
          _getMenu(names.head)
      menu.setVisible(true)
      val item = new MenuItem()
      UIString.get(name) match {
        case Some(uiString) =>
          uiString += item.textProperty()
        case None =>
          item.setText(caption)
      }
      item.setId(name.replace('.', '-'))
      item.setOnAction(_ => action.run())
      menu.getItems.add(item)
    }

  /**
   * This method is called to inform the menu manager about a change of focus so that it can update menu entries
   * where appropriate (enabling/disabling clipboard actins, for instance).
   */
  def focusChanged(node: Node): Unit

  /**
   * Creates the main menu to be displayed at the top of the window (or system).
   */
  def createMenu: MenuBar

  /**
   * Creates a menu `tools` for plugins, etc.
   */
  protected def createToolsMenu: Menu

  val mainMenu: MenuBar = createMenu
}
