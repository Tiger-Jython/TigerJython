/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.tabs

import tigerjython.ui.{TigerJythonApplication, editor}

/**
 * @author Tobias Kohn
 */
class NewDocumentItem extends DocumentItem {

  {
    titleLabel.setText("New Document")
    descriptionLabel.setText("Create a new empty document")
  }

  def onClicked(): Unit =
    TigerJythonApplication.tabManager.addTab(editor.PythonEditorTab())
}
