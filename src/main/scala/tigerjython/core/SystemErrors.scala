/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.core

import javax.swing.JOptionPane

/**
 * @author Tobias Kohn
 */
object SystemErrors {

  /**
   * Prints the given error message to the standard error output, displays a swing dialog and then terminates the
   * program with error code 1.
   *
   * @param msg The error message to be displayed.
   */
  def fatalError(msg: String): Unit = {
    System.err.println("ERROR: " + msg)
    JOptionPane.showMessageDialog(
      null,
      msg,
      "TigerJython: Fatal Error",
      JOptionPane.ERROR_MESSAGE)
    System.exit(1)
  }
}
