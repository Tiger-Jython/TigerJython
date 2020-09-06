/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

import java.nio.file.{InvalidPathException, Path, Paths}

import tigerjython.core.Configuration

/**
 * When running a JAR (instead of a binary), we must invoke `java` as the actual command and give the program to
 * execute as an argument.
 *
 * @author Tobias Kohn
 */
class PythonJavaProcess(val pythonCmd: String) extends InterpreterProcess("java") {

  def this(pythonCmd: Path) =
    this(pythonCmd.toAbsolutePath.toString)

  private val internedPython =
    try {
      Paths.get(Configuration.sourcePath).compareTo(Paths.get(pythonCmd)) == 0
    } catch {
      case _: InvalidPathException =>
        Configuration.sourcePath.toString == pythonCmd
      case _: NoSuchMethodError =>
        Configuration.sourcePath.toString == pythonCmd
    }

  override protected def createCommand(args: Seq[String]): Array[String] =
    if (internedPython)
      Array("java", "-jar", pythonCmd, "-jython") :++ args
    else
      Array("java", "-jar", pythonCmd, "-S") :++ args
}
