/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.jython

import org.python.core.{Py, PyObject}
import tigerjython.core.{BuildInfo, Configuration, Preferences}

/**
 *
 *
 * @author Tobias Kohn
 */
object TigerJythonBuiltins {

  /**
   * Returns preference values for specific settings.
   *
   * Many of the settings here reflect the history of TigerJython and are due to a large body of additional libraries.
   *
   * If no value can be returned (either because of a missing setting, or because the key is undefined), `Py.None` is
   * returned instead.
   *
   * @param name   The name/key of the setting to query.
   * @return       Either the value as stored in the settings/preferences, or `Py.None`.
   */
  def getTigerJythonSeting(name: String): PyObject =
    name.toLowerCase match {
      case "aplu.device.ip" =>
        Py.None
      case "gpanel.windowsize" =>
        Py.None
      case "gturtle.hideonstart" =>
        Py.None
      case "gturtle.playground.height" =>
        Py.None
      case "gturtle.playground.width" =>
        Py.None
      case "gturtle.windowsize" | "gturtle.window.size" | "gturtle.playgroundsize" | "gturtle.playground.size" =>
        Py.None
      case "jar" =>
        Py.newString(Configuration.sourcePath.getPath)
      case "lang" | "language" | "tigerjython.language" =>
        Py.newString(Preferences.languageCode.get)
      case "path" =>
        Py.newString(Configuration.sourcePath.getPath)
      case "tigerjython.version" =>
        Py.newString(BuildInfo.Version)
      case _ =>
        Py.None
    }
}
