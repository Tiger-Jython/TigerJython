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

/**
 * The Python-Installations holds a list of all installations on the system, from which the user can choose one to
 * run the Python programs.
 *
 * @author Tobias Kohn
 */
object PythonInstallations {

  private final val APP_NAME = "Jython (%s)".format(org.python.Version.PY_VERSION)

  private val _availableVersions = collection.mutable.ArrayBuffer[String](
    APP_NAME
  )
  private val _executablePaths = collection.mutable.Map[String, Path](
    APP_NAME -> getInternalJythonPath
  )
  private var _selectedIndex: Int = 0

  /**
   * Get the path of the internal Jython interpreter for execution.
   */
  private def getInternalJythonPath: Path = {
    val cls = classOf[org.python.util.PythonInterpreter]
    val path = cls.getProtectionDomain.getCodeSource.getLocation.toURI
    try {
      Path.of(path)
    } catch {
      case _: NoSuchMethodError =>
        Paths.get(path)
      case _: Throwable =>
        null
    }
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
    if (version == APP_NAME) {
      var i = 1
      while (_executablePaths.contains("%s (%d)".format(APP_NAME, i)))
        i += 1
      add("%s (%d)".format(APP_NAME, i), path)
    } else if (version != null && path != null) {
      _availableVersions += version
      _executablePaths(version) = path
      val p = Preferences.userNodeForPackage(getClass).node("Python")
      if (p != null)
        p.put(version, path.toAbsolutePath.toString)
    }
  }

  def add(version: String, path: String): Unit =
    add(version, Path.of(path))

  private def addVersions(versions: Array[(String, Path)]): Unit =
    synchronized {
      for ((version, path) <- versions)
        if (!_executablePaths.contains(version)) {
          _availableVersions += version
          _executablePaths(version) = path
        }
    }

  def getAvailableVersions: Array[String] = _availableVersions.toArray

  def getSelectedIndex: Int = _selectedIndex

  def getSelectedPath: Path = _executablePaths(getSelectedVersion)

  def getSelectedVersion: String = _availableVersions(_selectedIndex)

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

  private def readPreferences(): Unit = {
    val p = Preferences.userNodeForPackage(getClass).node("Python")
    if (p != null) {
      val versions = collection.mutable.ArrayBuffer[(String, Path)]()
      for (version <- p.keys()) {
        val path = p.get(version, null)
        if (path != null)
          versions += ((version, Path.of(path)))
      }
      addVersions(versions.toArray)
    }
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
