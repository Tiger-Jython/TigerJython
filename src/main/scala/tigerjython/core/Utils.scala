/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.core

import tigerjython.configparser._

/**
 * Various small utility and helper functions.
 *
 * @author Tobias Kohn
 */
object Utils {

  /**
   * Finds the full file name for a given resource file.
   */
  def findFile(fileName: String): String = fileName

  /**
   * Loads the specified file and returns a map that maps all defined names to their respective values.
   */
  def loadConfigFile(fileName: String): Map[String, ConfigValue] = {
    val source = scala.io.Source.fromFile(findFile(fileName))
    if (source != null)
      Parser.parse(source.getLines)
    else
      throw new RuntimeException("Could not load config file '%s'".format(fileName))
  }
}
