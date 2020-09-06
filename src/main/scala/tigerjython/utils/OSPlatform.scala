/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.utils

/**
 * An enumeration that holds the information on which operating system/platform we are running.
 *
 * @author Tobias Kohn
 */
object OSPlatform extends Enumeration {

  final val LINUX = Value("Linux")
  final val MAC_OS = Value("Mac OS")
  final val SOLARIS = Value("Solaris")
  final val UNIX = Value("Unix")
  final val WINDOWS = Value("Windows")
  final val UNKNOWN = Value("<Unknown>")

  lazy val system: Value = {
    val osName = System.getProperty("os.name").toLowerCase
    if (osName.contains("win"))
      WINDOWS
    else if (osName.contains("mac"))
      MAC_OS
    else if (osName.contains("linux"))
      LINUX
    else if (osName.contains("sunos"))
      SOLARIS
    else if (osName.contains("nix"))
      UNIX
    else
      UNKNOWN
  }

  def isWindows: Boolean = system == WINDOWS
}
