/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.preferences

import javafx.beans.property.StringProperty
import javafx.scene.Node

/**
 * The preference settings are split up into individual panes, allowing for better user orientation, but also to add
 * custom preferences for libraries easily.  Each pane to be displayed in the preferences implements this interface,
 * providing both a caption to be displayed, as well as the contents.
 *
 * @author Tobias Kohn
 */
trait PreferencePane {

  def caption: StringProperty

  def node: Node
}
