/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

import javafx.scene.image.Image
import tigerjython.ui.ImagePool

/**
 * The Interpreter-Installations holds a list of all installations on the system, from which the user can choose one to
 * run the programs.
 *
 * @author Tobias Kohn
 */
object InterpreterInstallations {

  case class InterpreterInfo(title: String, icon: Image, factory: ExecutorFactory)

  final val Separator = InterpreterInfo("-", null, null)

  private var cache: Array[InterpreterInfo] = _

  def availableInterpreters: Array[InterpreterInfo] = synchronized {
    if (cache == null)
      cache = getAvailableInterpreters
    cache
  }

  /**
   * Invalidate the cache and therefore force the list of available interpreters to be re-evaluated.
   */
  def invalidateCache(): Unit = {
    cache = null
  }

  private def getAvailableInterpreters: Array[InterpreterInfo] = {
    /*
     * We first collect all the Python interpreters and then sort and filter them.  TigerJython shall always be the
     * first entry.  If there are entries for `Python 3.6` and `Python 3.6.4`, only the latter is kept and the first
     * is seen as a link to the second.  The same goes for `Python 3` and `Python 3.6`.
     *
     * This code also assigns a logo/icon to each interpreter.
     */
    val result = collection.mutable.ArrayBuffer[InterpreterInfo]()
    val interpreters = collection.mutable.ArrayBuffer[InterpreterInfo]()
    for ((name, version, path) <- PythonInstallations.getAvailableSystems)
      name.takeWhile(_.isLetter).toLowerCase match {
        case "tigerjython" =>
          result += InterpreterInfo(name, ImagePool.tigerJython_Logo, TigerJythonExecutorFactory)
        case "jython" =>
          val factory = new CommandExecutorFactory(name, path, ExecLanguage.python(version))
          interpreters += InterpreterInfo(name, ImagePool.jython_Logo, factory)
        case "pypy" =>
          val factory = new CommandExecutorFactory(name, path, ExecLanguage.python(version))
          interpreters += InterpreterInfo(name, ImagePool.pypy_Logo, factory)
        case "python" =>
          if (version < 3)
            interpreters += InterpreterInfo(name, ImagePool.python2_Logo,
              new CommandExecutorFactory(name, path, ExecLanguage.PYTHON_2))
          else
            interpreters += InterpreterInfo(name, ImagePool.python_Logo,
              new CommandExecutorFactory(name, path, ExecLanguage.PYTHON_3))
        case _ =>
          interpreters += InterpreterInfo(name, ImagePool.python2_Logo,
            new CommandExecutorFactory(name, path, if (version < 3) ExecLanguage.PYTHON_2 else ExecLanguage.PYTHON_3))
      }
    if (result.nonEmpty)
      result += Separator
    interpreters.sortInPlaceBy(_.title)
    for (i <- 0 until interpreters.length-1) {
      val intp = interpreters(i)
      if (!interpreters(i+1).title.startsWith(intp.title))
        result += intp
    }
    if (interpreters.nonEmpty) {
      result += interpreters.last
      result += Separator
    }

    // Add other devices that are not directly detectable
    result += InterpreterInfo("Micro:bit", ImagePool.microBit_Logo,
      new MicroDeviceExecutorFactory("Micro:bit", ExecLanguage.PYTHON_3))
    result += InterpreterInfo("Calliope Mini", ImagePool.calliope_Logo,
      new MicroDeviceExecutorFactory("Calliope Mini", ExecLanguage.PYTHON_3))
    result.toArray
  }
}
