/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.core

import java.io.InputStream

import org.python.core.Options
import org.python.core.Py
import org.python.core.util.CustomModuleFinder
import org.python.util.PythonInterpreter
import tigerjython.jython
import tigerjython.remote.ExecuteClientConnection

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
    Py.getSystemState.custom_module_finder = new TigerJythonModuleFinder()
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

  def runRemote(): Unit = {
    interpreter = new PythonInterpreter()
    while (true) {
      ExecuteClientConnection.processMessage(interpreter)
      Thread.sleep(100)
    }
  }

  /**
   * A simple (and sneaky) way for overriding Jython's import mechanism.  `name` is the simple name and `moduleName`
   * the fully qualified name of the module to import.  Return `null` to have Jython go for its standard import
   * mechanism.
   */
  private class TigerJythonModuleFinder extends CustomModuleFinder {

    /**
     * Return the path where the module is to be found as `$path/$name.py`.
     *
     * @param name       The module's 'simple' name.
     * @param moduleName The fully qualified name of the module.
     * @return           The name of the path or `null`.
     */
    override def find_module(name: String, moduleName: String): String =
      null

    /**
     * Return an input-stream providing the source code for the module.
     *
     * @param name       The module's 'simple' name.
     * @param moduleName The fully qualified name of the module.
     * @return           An inputstream providing the source code or `null`.
     */
    override def provide_module_code(name: String, moduleName: String): InputStream = {
      null
    }
  }
}
