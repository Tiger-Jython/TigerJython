/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.core

import tigerjython.utils.OSPlatform

/**
 * This is the main entry point for the application.
 *
 * It actually acts as a bootstrap that initialises the system, handles any arguments passed, and then starts the
 * user interface (if appropriate).
 *
 * @author Tobias Kohn
 */
object TigerJython {

  /**
   * There are several command-line options available:
   *
   * - `-jython` invokes the internal Jython interpreter instead of starting the IDE.  It needs to be followed by
   *   a filename indicating the Python-script to execute.
   */
  def main(args: Array[String]): Unit = {
    if (args.nonEmpty && args.head == "-jython") {
      val files = args.filter(!_.startsWith("-"))
      if (files.length == 1)
        JythonExecutor.run(files.head)
      else if (files.length < 1) {
        System.err.println("Error: missing filename to execute")
      } else {
        System.err.println("Error: too many filenames to execute:")
        System.err.println(args.mkString(" "))
      }
    } else {
      println("TigerJython " + BuildInfo.fullVersion)
      println("  on Java " + System.getProperty("java.version"))
      println("  on " + OSPlatform.system.toString)

      tigerjython.execute.PythonInstallations.initialize()
      tigerjython.core.Configuration.initialize()
      tigerjython.ui.TigerJythonApplication.launchApplication(args)
    }
  }
}