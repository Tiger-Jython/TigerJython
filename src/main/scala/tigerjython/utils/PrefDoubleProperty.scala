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
class PrefDoubleProperty(protected val preferences: JPreferences, val name: String, default: Double = 0.0) extends
      SimpleDoubleProperty(preferences.getDouble(name, default)) {

  addListener(new ChangeListener[Number] {
    override def changed(observableValue: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit =
      preferences.putDouble(name, newValue.doubleValue())
  })
}
