/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

import java.nio.file.{Path, Paths}
import java.util.prefs.Preferences
import tigerjython.ui.Utils
import tigerjython.utils.OSProcess

/**
 * The Python-Installations holds a list of all installations on the system, from which the user can choose one to
 * run the Python programs.
 *
 * @author Tobias Kohn
 */
object PythonInstallations {

  private final val JYTHON_NAME = "TigerJython (%s)".format(org.python.Version.PY_VERSION)
  private final val JYTHON_VERSION: Int = org.python.Version.PY_MAJOR_VERSION

  private val _availableVersions = collection.mutable.ArrayBuffer[String](
    JYTHON_NAME
  )
  private val _executablePaths = collection.mutable.Map[String, (Int, Path)](
    JYTHON_NAME -> (JYTHON_VERSION, getInternalJythonPath)
  )
  private var _selectedIndex: Int = 0

  /**
   * This list contains all interpreters that were added by the user (rather than being detected automatically).
   */
  private val _customInterpreters = collection.mutable.ArrayBuffer[String]()

  /**
   * Get the path of the internal Jython interpreter for execution.
   */
  def getInternalJythonPath: Path = {
    val cls = classOf[org.python.util.PythonInterpreter]
    val path = cls.getProtectionDomain.getCodeSource.getLocation.toURI
    try {
      Paths.get(path)
    } catch {
      case _: Throwable =>
        null
    }
  }

  /**
   * Extracts the version number from the version string (which is something like `python3` or `Python 3.6`).
   */
  private def extractVersion(versionString: String): Int = {
    val s = versionString.dropWhile(!_.isDigit).takeWhile(_.isDigit)
    if (s.nonEmpty && (s(0) == '2' || s(0) == '3'))
      s(0).toInt - '0'
    else
      0
  }

  /**
   * Adds another Python version to choose from for running Python programs.  This also allows to overwrite other
   * Python installations already registered---except for "TigerJython" itself, which is protected and will cause
   * the addition to be renamed.
   *
   * All added Python installations will be stored persistently in the preferences.  It is therefore not a good idea
   * to store versions discovered automatically, since these could change (e.g., through updates).
   *
   * @param version   The version displayed to the user (a string like `"Python 3.9"`, say).
   * @param path      The path to the executable.
   */
  def add(version: String, path: Path): Unit = synchronized {
    if (version == JYTHON_NAME) {
      var i = 1
      while (_executablePaths.contains("%s (%d)".format(JYTHON_NAME, i)))
        i += 1
      add("%s (%d)".format(JYTHON_NAME, i), path)
    } else if (version != null && path != null) {
      _availableVersions += version
      _executablePaths(version) = (extractVersion(version), path)
      val p = Preferences.userNodeForPackage(getClass).node("Python")
      if (p != null)
        p.put(version, path.toAbsolutePath.toString)
    }
  }

  def add(version: String, path: String): Unit =
    add(version, Paths.get(path))

  def add(file: java.io.File, onSuccess: ()=>Unit = null): Unit =
    if (file.exists() && file.canExecute) {
      val process = new OSProcess(file)
      process.exec("--version")
      val output = process.waitForOutput()
      if (output.length > 8 && output.takeWhile(_.isLetter).endsWith("ython")) {
        val name = output.takeWhile(_ >= ' ')
        add(name, file.toPath)
        _customInterpreters += name
        select(name)
        if (onSuccess != null)
          onSuccess()
      } else
        Utils.alertError("This does not seem to be a Python interpreter: '%s'".format(file.getCanonicalPath))
    } else
      Utils.alertError("Could not find the executable '%s'".format(file.getCanonicalPath))

  private def addVersions(versions: Array[(String, Path)]): Unit =
    synchronized {
      for ((version, path) <- versions)
        if (!_executablePaths.contains(version)) {
          _availableVersions += version
          _executablePaths(version) = (extractVersion(version), path)
        }
    }

  def getAvailableSystems: Array[(String, Int, Path)] = {
    val result = collection.mutable.ArrayBuffer[(String, Int, Path)]()
    for (version <- _availableVersions) {
      val (v, p) = _executablePaths(version)
      result += ((version, v, p))
    }
    result.toArray
  }

  def getAvailableVersions: Array[String] = _availableVersions.toArray

  def getSelectedIndex: Int = _selectedIndex

  def getSelectedPath: Path = _executablePaths(getSelectedCaption)._2

  def getSelectedCaption: String = _availableVersions(_selectedIndex)

  def getSelectedVersionNumber: Int = _executablePaths(getSelectedCaption)._1

  /**
   * Loads previously saved preferences and tries to discover any installed Python versions on the system.
   */
  def initialize(): Unit = {
    readPreferences()
    new Thread(() => {
      val discoveredVersions = PythonDetective.discover()
      addVersions(discoveredVersions)
      selectFromPreferences()
    }).start()
  }

  def isCustomInterpreter(name: String): Boolean =
    _customInterpreters.contains(name)

  private def readPreferences(): Unit = {
    val p = Preferences.userNodeForPackage(getClass).node("Python")
    if (p != null) {
      val versions = collection.mutable.ArrayBuffer[(String, Path)]()
      for (version <- p.keys()) {
        val path = p.get(version, null)
        if (path != null) {
          versions += ((version, Paths.get(path)))
          _customInterpreters += version
        }
      }
      addVersions(versions.toArray)
    }
  }

  def removeCustomInterpreter(name: String): Unit =
    if (name != null && name != "" && _customInterpreters.contains(name)) {
      _customInterpreters.remove(_customInterpreters.indexOf(name))
      val p = Preferences.userNodeForPackage(getClass).node("Python")
      if (p != null)
        p.remove(name)
      _availableVersions.remove(_availableVersions.indexOf(name))
      _executablePaths.remove(name)
    }

  def select(index: Int): Unit =
    if (0 <= index && index < _availableVersions.length) {
      _selectedIndex = index
      val p = Preferences.userNodeForPackage(getClass)
      if (p != null)
        p.put("selected", _availableVersions(index))
    }

  def select(version: String): Unit =
    select(_availableVersions.indexOf(version))

  private def selectFromPreferences(): Unit = {
    val p = Preferences.userNodeForPackage(getClass)
    if (p != null) {
      val selected = p.get("selected", null)
      if (selected != null)
        select(selected)
    }
  }

  def useInternal: Boolean =
    false // _selectedIndex == 0
}
