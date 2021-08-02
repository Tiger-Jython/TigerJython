/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui

import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType

/**
 * @author Tobias Kohn
 */
object Dialogs {

  /**
   * This dialog is shown when the target device (i.e. Micro:bit or Calliope) could not be found automatically.  It
   * can give the user a chance to select or enter the path manually.
   *
   * @param  deviceName   The name of the device, e.g., `"Micro:bit"` or `"Calliope"`.
   * @return              If the user has entered a valid path, returns `Some(path)`, otherwise `None`.
   */
  def selectMicroDeviceTarget(deviceName: String): Option[String] = {
    val alert = new Alert(AlertType.ERROR, "Could not find the target path for the %s device.".format(deviceName))
    alert.showAndWait()
    None
  }
}
