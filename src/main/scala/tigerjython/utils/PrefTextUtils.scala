/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.utils

import java.util.prefs.{Preferences => JPreferences}

/**
 * Strings in a preference file are limited to around 8 KB.  Larger strings therefore need to be split up and saved
 * piecewise.
 *
 * Given a key/name for a property, the entire text is split up and saves as pieces like:
 *  %key%.count = 2    // number of parts
 *  %key%._0 = "..."   // first part
 *  %key%._1 = "..."   // second part
 *
 * In case no `%key%.count` field is present, the function will try to load `%key%` directly, instead.  This allows for
 * a smooth transition from "String"- to "Text"-properties.
 *
 * @author Tobias Kohn
 */
object PrefTextUtils {

  val MAX_STRING_LENGTH: Int = JPreferences.MAX_VALUE_LENGTH

  def loadTextFromPreference(preferences: JPreferences, name: String): String = {
    val count = preferences.getInt(name + ".count", 0) max 0
    if (count == 0)
      return preferences.get(name, "")
    val result = new Array[String](count)
    for (i <- result.indices)
      result(i) = preferences.get("%s._%d".format(name, i), "")
    result.mkString("")
  }

  def saveTextToPreferences(preferences: JPreferences, name: String, text: String): Unit =
    if (text != null || text != "") {
      val old_count = preferences.getInt(name + ".count", 0)
      var count = 0
      var i = 0
      while (i < text.length) {
        val txt = if (i + MAX_STRING_LENGTH < text.length)
          text.substring(i, i + MAX_STRING_LENGTH)
        else
          text.substring(i)
        preferences.put("%s._%d".format(name, count), txt)
        i += MAX_STRING_LENGTH
        count += 1
      }
      preferences.putInt(name + ".count", count)
      preferences.putInt(name + ".length", text.length)
      while (count < old_count) {
        preferences.remove("%s._%d".format(name, count))
        count += 1
      }
    } else {
      preferences.putInt(name + ".count", 0)
      preferences.putInt(name + ".length", 0)
      preferences.put(name, "")
      val n = "%s._".format(name)
      for (key <- preferences.keys() if key.startsWith(n))
        preferences.remove(key)
    }
}
