/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.core

import tigerjython.plugins.{EventManager, MainWindow}
import tigerjython.ui.TigerJythonApplication
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
   * Returns the event manager which allows to register listening to events.
   */
  def getEventManager: EventManager.type = EventManager

  /**
   * Returns the `MainWindow`, which is an interface for plugins to access the main window, add menu entries, etc.
   */
  def getMainWindow: MainWindow = TigerJythonApplication.mainWindow

  /**
   * There are several command-line options available:
   *
   * - `-jython` invokes the internal Jython interpreter instead of starting the IDE.  It needs to be followed by
   *   a filename indicating the Python-script to execute.
   */
  def main(args: Array[String]): Unit = {
    if (args.nonEmpty)
      args.head match {
        case "-jython" =>
          val files = args.filter(!_.startsWith("-"))
          if (files.length == 1)
            JythonExecutor.run(files.head)
          else if (files.length < 1) {
            System.err.println("Error: missing filename to execute")
          } else {
            System.err.println("Error: too many filenames to execute:")
            System.err.println(args.mkString(" "))
          }
        case "-client" =>
          val port = args(1).toInt
          val id = args(2).toInt
          tigerjython.remote.ExecuteClientConnection.initialize(port, id)
          JythonExecutor.initialize()
        case _ =>
          startEditor(args)
      }
    else
      startEditor(args)
  }

  private def startEditor(args: Array[String]): Unit = {
    println("TigerJython " + BuildInfo.fullVersion)
    println("  on Java " + Configuration.getJavaVersion.toString)
    println("  on " + OSPlatform.system.toString)

    tigerjython.execute.PythonInstallations.initialize()
    tigerjython.core.Configuration.initialize()
    tigerjython.remote.ExecuteServer.initialize()
    tigerjython.ui.TigerJythonApplication.launchApplication(args)
  }
}