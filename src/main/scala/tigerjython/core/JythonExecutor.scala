/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.core

import org.python.core.Options
import org.python.util.PythonInterpreter
import tigerjython.jython

/**
 * The Jython executor is responsible for invoking Jython and executing the Python file as specified by the filename.
 *
 * @author Tobias Kohn
 */
object JythonExecutor {

  var interpreter: PythonInterpreter = _

  def initialize(): Unit = {
    Options.importSite = false
    Options.Qnew = true
    val postProperties = new java.util.Properties()
    postProperties.setProperty("python.cachedir.skip", "false")
    PythonInterpreter.initialize(System.getProperties, postProperties, null)
    jython.JythonBuiltins.initialize()
  }

  /**
   * Run the given file using the internally packaged Jython.
   *
   * @param filename  The name or full path of the file to execute.
   */
  def run(filename: String): Unit = {
    initialize()
    interpreter = new PythonInterpreter()
    interpreter.execfile(filename)
  }
}
