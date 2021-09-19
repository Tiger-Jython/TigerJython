/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

import java.nio.file.{Files, Path, Paths}
import java.util.prefs.Preferences

import tigerjython.utils.{OSPlatform, OSProcess}

/**
 * The "Python-Detective" tries to discover Python installations on your system.
 *
 * Note that we do not go out of our way to find all possible Python installations.  This is not a crucial part of
 * TigerJython, but rather a backup.  As the user has the opportunity to register additional Python-versions manually,
 * it suffices if we cover the most common cases.
 *
 * @author Tobias Kohn
 */
object PythonDetective {

  /**
   * We need a work-around for older Java-versions because `stripTrailing` is a rather new method.
   */
  private def stripTrailing(s: String): String = {
    var i = s.length
    while (i > 0 && s(i-1).isWhitespace)
      i -= 1
    s.take(i)
  }
    /*try {
      s.stripTrailing()
    } catch {
      case _: NoSuchMethodError =>
        var i = s.length
        while (i > 0 && s(i-1).isWhitespace)
          i -= 1
        s.take(i)
    }*/

  /**
   * Helper for the `discover`-method: looks for Python on Linux and related operating systems.
   *
   * On Linux, we just look for Python-executables in the path `/usr/bin`.
   *
   * This search is based on the assumption that there are executables for Python installed there, which bear names
   * according to the scheme "python" + version, e.g., `python3.7`.  There might also be a symbolic link with the
   * name "python3" that then refers to a standard Python version.
   */
  private def discoverOnUnix(): Array[(String, Path)] = {
    val pythonFiles = collection.mutable.ArrayBuffer[(String, Path)]()
    var standard: String = ""
    for (rootPath <- Array("/usr/bin"))
      Files.walk(Paths.get(rootPath)).forEach(file => {
        Files.isExecutable(file)
        val filename = file.getFileName.toString
        val versionString = filename.drop(6)
        if (filename.startsWith("python") && versionString.forall(x => x.isDigit || x == '.')) {
          if (Files.isSymbolicLink(file)) {
            val targetName = Files.readSymbolicLink(file).getFileName.toString
            val targetVersion = targetName.drop(6)
            if (targetName.startsWith("python3") && targetVersion.forall(x => x.isDigit || x == '.'))
              standard = "Python " + targetVersion
          } else
            pythonFiles += (("Python " + versionString, file))
        } else
        filename match {
          case "jython" =>
            pythonFiles += (("Jython", file))
          case "pypy" =>
            pythonFiles += (("PyPy", file))
          case "pypy3" =>
            pythonFiles += (("PyPy 3", file))
          case _ =>
        }
      })
    // If the system has a default Python 3 version, we put it to the top of our list
    for (i <- pythonFiles.indices)
      if (pythonFiles(i)._1 == standard) {
        val default = pythonFiles.remove(i)
        pythonFiles.insert(0, default)
      }
    pythonFiles.toArray
  }

  /**
   * On Windows, we acquire the information about installed Python version from the registry.  Reading the registry,
   * however, is a bit tricky under Java.  While the Java-Preferences sometimes map to the registry, giving us some
   * quick and convenient access, we will usually have to rely on the command line tool `reg.exe` and query the
   * registry with it (which means that we have to parse the output).
   *
   * The structure in the registry is according to PEP 514 (https://www.python.org/dev/peps/pep-0514/).  Unfortunately,
   * this means that we cannot detect Python versions older than 3.5 with this scheme.
   */
  private def discoverOnWindows(): Array[(String, Path)] = {
    val pythonFiles = collection.mutable.ArrayBuffer[(String, String)]()

    // In principle, the preferences might actually map to the Windows registry.  In that case, we can use the
    // preferences to read the available Python versions.
    val root = Preferences.userRoot()
    for (rootName <- Array(
      "HKEY_CURRENT_USER/Software/Python/PythonCore",
      "HKEY_LOCAL_MACHINE/Software/Python/PythonCore"
    ))
      if (root.nodeExists(rootName)) {
        val pyCore = root.node(rootName)
        for (name <- pyCore.childrenNames())
          if (pyCore.node(name).nodeExists("PythonPath")) {
            val path = pyCore.node(name).node("PythonPath").get("ExecutablePath", null)
            if (path != null)
              pythonFiles += ((name, path))
          }
      }

    // Otherwise, we have to try with `reg.exe` to query the registry and parse its output.
    if (pythonFiles.isEmpty) {
      val process = new OSProcess("reg.exe")
      process.exec("query", "HKCU\\Software\\Python\\PythonCore", "/s", "/f", "python.exe")
      val output = process.waitForOutput()
      if (output != null) {
        val lines = output.split("\r\n").filter(_.length > 0)
        var versionLine: String = null
        for (line <- lines)
          if (line.contains("REG_SZ")) {
            val index = line.indexOf("REG_SZ")
            val path = stripTrailing(line.drop(index + 6).dropWhile(_ <= ' '))
            pythonFiles += ((versionLine, path))
          } else if (line.startsWith("HKEY_CURRENT_USER\\") && line.contains("\\PythonCore\\")) {
            val index = line.indexOf("\\PythonCore\\")
            versionLine = line.drop(index + 12).takeWhile(_ != '\\')
          }
      }
    }
    // Parse the version numbers, which are of the form `"37_64"` for 64-bit Python 3.7, say.
    if (pythonFiles.nonEmpty) {
      val result = new Array[(String, Path)](pythonFiles.length)
      for (i <- result.indices) {
        val (versionString, pathName) = pythonFiles(i)
        val versionStrings = versionString.split('_')
        if (versionStrings.nonEmpty && versionStrings.head.startsWith("3")) {
          val version =
            if (versionStrings.head.contains('.'))
              versionStrings.head
            else
              "3." + versionStrings.head.drop(1)
          if (versionStrings.length > 1)
            version + " (%s-bit)".format(versionStrings(1))
          result(i) = ("Python " + version, Paths.get(pathName))
        }
      }
      result
    } else
      Array()
  }

  def discover(): Array[(String, Path)] =
    OSPlatform.system match {
      case OSPlatform.LINUX | OSPlatform.UNIX =>
        discoverOnUnix()
      case OSPlatform.WINDOWS =>
        discoverOnWindows()
      case OSPlatform.MAC_OS =>
        discoverOnUnix()
      case _ =>
        Array()
    }
}
