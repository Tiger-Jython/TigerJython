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
 * Strings in a preference file are limited to around 8 KB (according to the Java docs).  Larger strings therefore need
 * to be split up and saved piecewise.  This is done automatically by `PrefTextProperty` (but not by
 * `PrefStringProperty`).
 *
 * @author Tobias Kohn
 */
class PrefTextProperty(protected val preferences: JPreferences, val name: String) extends
  SimpleStringProperty(PrefTextUtils.loadTextFromPreference(preferences, name)) {

  addListener(new ChangeListener[String] {
    override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit =
      PrefTextUtils.saveTextToPreferences(preferences, name, newValue)
      //preferences.put(name, newValue)
  })
}