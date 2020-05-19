/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.preferences

import javafx.scene.control.{Accordion, TitledPane}
import javafx.scene.layout.StackPane
import tigerjython.ui.TabFrame

/**
 * This the preference frame that contains all the different individual preference panes and is displayed in the main
 * window.
 *
 * @author Tobias Kohn
 */
class PreferencesTab protected () extends TabFrame {

  val panes: Array[PreferencePane] = createPanes

  {
    val mainBox = new Accordion()
    for (pane <- panes) {
      val titlePane = new TitledPane()
      titlePane.setContent(new StackPane(pane.node))
      titlePane.setText(pane.caption)
      mainBox.getPanes.add(titlePane)
    }
    mainBox.prefWidthProperty.bind(widthProperty())
    mainBox.setExpandedPane(mainBox.getPanes.get(0))
    getChildren.add(mainBox)
    caption.setValue("Preferences")
  }

  def createPanes: Array[PreferencePane] = {
    Array(
      new GeneralPreferencesPane(),
      new AppearancePreferencesPane(),
      new PythonPreferencesPane()
    )
  }
}
object PreferencesTab {

  private lazy val _preferencesTab: PreferencesTab = new PreferencesTab()

  def apply(): PreferencesTab = _preferencesTab
}