/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.utils

import java.util.prefs.{Preferences => JPreferences}
import javafx.beans.property._
import javafx.beans.value.{ChangeListener, ObservableValue}

/**
 * @author Tobias Kohn
 */
class PrefStringProperty(protected val preferences: JPreferences, val name: String, default: String = null) extends
      SimpleStringProperty(preferences.get(name, default)) {

  addListener(new ChangeListener[String] {
    override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit =
      preferences.put(name, newValue)
  })
}
