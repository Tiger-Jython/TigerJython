/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.editor

import javafx.scene.control.Tab

/**
 * There are usually at least two output panes: one for standard output, and one for errors.  The output pane
 * displays simple text, but it might also provide the means to enter some text, which can then be sent to the
 * running process as user input.
 *
 * @author Tobias Kohn
 */
trait OutputPane extends Tab {

  var onKeyPress: Char=>Unit

  def append(s: String): Unit

  def clear(): Unit

  def getContentText: String
}
