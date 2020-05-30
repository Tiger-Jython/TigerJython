/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.jython

import java.lang.reflect.Method
import org.python.core._

/**
 * We override some of Jython's built-in functions and objects.  The actual built-in functions are defined as
 * static functions in `Builtins`.
 *
 * @author Tobias Kohn
 */
object JythonBuiltins {

  /**
   * This is the list of all names of methods to be redefined.
   */
  val builtinNames: Array[String] = Array(
    "getTigerJythonFlag",
    "input", //"inputFloat",
    //"inputInt", "inputString",
    "isInteger",
    "makeColor", "msgDlg", "raw_input",
  )

  /**
   * Creates a new Python function object `PyReflectedFunction` and binds it to `null` as instance object, so that it
   * can be called like any other Python function.
   */
  private def createPyFunction(name: String): PyObject = {
    val methods = collection.mutable.ArrayBuffer[Method]()
    for (method <- classOf[Builtins].getMethods)
      if (method.getName == name)
        methods += method
    new PyReflectedFunction(methods.toSeq: _*)._doget(null)
  }

  def initialize(): Unit =
    Py.getSystemState.getBuiltins match {
      case map: PyStringMap =>
        for (name <- builtinNames)
          map.__setitem__(Py.newString(name), createPyFunction(name))
        Py.getSystemState.setBuiltins(map)
      case _ =>
    }
}
