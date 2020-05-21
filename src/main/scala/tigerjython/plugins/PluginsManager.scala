/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.plugins

import java.io.{FileInputStream, InputStream}
import java.nio.file.Paths

import org.python.core.{Options, Py}
import org.python.util.PythonInterpreter
import tigerjython.core.Configuration
import tigerjython.jython

/**
 * The Plugin-Manager loads any plugins.
 *
 * @author Tobias Kohn
 */
object PluginsManager {

  private class PythonInitializer(val resources: Array[InputStream]) extends Runnable {

    def run(): Unit = {
      Options.importSite = false
      Options.Qnew = true
      PythonInterpreter.initialize(System.getProperties, null, null)
      if (Configuration.sourcePath.toString.endsWith(".jar")) {
        try {
          val jarPath = Paths.get(Configuration.sourcePath)
          val sysPaths = Py.getSystemState.path
          sysPaths.append(Py.newString(jarPath.resolve("plugins").toString))
          sysPaths.append(Py.newString(jarPath.getParent.resolve("plugins").toString))
        } catch {
          case _: NoSuchMethodError =>
            Py.getSystemState.path.append(Py.newString(Configuration.sourcePath.getPath + "/plugins"))
        }
      } else
        if (Configuration.sourcePath.toString.endsWith("/"))
          Py.getSystemState.path.append(Py.newString(Configuration.sourcePath.getPath + "plugins"))
      jython.JythonBuiltins.initialize()
      val interpreter = new PythonInterpreter()
      for (resource <- resources)
        interpreter.execfile(resource, "plugins/__init__.py")
    }
  }

  def initialize(): Unit = {
    val resources = collection.mutable.ArrayBuffer[InputStream]()
    val resource = getClass.getClassLoader.getResourceAsStream("plugins/__init__.py")
    if (resource != null)
      resources += resource
    // When dealing with path objects, we have to be careful:
    // If something fails, we do not want to crash the entire system.
    try {
      val extFile = Paths.get(Configuration.sourcePath).getParent.resolve("plugins/__init__.py").toFile
      if (extFile.exists()) {
        val stream = new FileInputStream(extFile)
        if (stream != null)
          resources += stream
      }
    } catch {
      case _: NoSuchMethodError =>
    }
    new Thread(new PythonInitializer(resources.toArray)).start()
  }
}
