/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui

import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.scene.layout.Region

/**
 * A `TabFrame` is the basic interface for anything that is displayed in a separate tab in the main window.  It allows
 * any descendants to access the manager, but also makes sure that each frame provides contents and a caption to be
 * displayed to select the tab.
 *
 * @author Tobias Kohn
 */
trait TabFrame extends Region {

  private var _manager: TabManager = _

  val caption: StringProperty = new SimpleStringProperty()

  def close(): Unit =
    if (_manager != null)
      _manager.closeTab(this)

  def focusChanged(receiveFocus: Boolean): Unit = {}

  def manager: TabManager = _manager

  private[ui] def manager_=(m: TabManager): Unit =
    _manager = m
}
