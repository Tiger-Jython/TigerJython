/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui

import javafx.application.Platform
import javafx.beans.property.{ObjectProperty, StringProperty}
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, ButtonType, Tooltip}

/**
 * These utility and helper functions are merely an interface, but this objects allows to conveniently include the
 * functions in other code.
 *
 * @author Tobias Kohn
 */
object Utils {

  /**
   * Displays the message to the user.
   */
  def alert(message: String): Unit =
    onFX(() => {
      val alert = new Alert(AlertType.NONE, message, ButtonType.OK)
      alert.showAndWait()
    })

  /**
   * Informs the user about an error that has occurred.
   */
  def alertError(message: String): Unit =
    onFX(() => {
      val alert = new Alert(AlertType.ERROR, message, ButtonType.OK)
      alert.showAndWait()
    })

  /**
   * Binds the given string property to a specific "user interface" string.  This allows to directly change all such
   * UI captions quickly (e.g. when changing the application's language).
   */
  def setUICaption(captionID: String, property: StringProperty): Unit =
    UIString(captionID + ".caption") += property

  def setUITooltip(captionID: String, property: ObjectProperty[Tooltip]): Unit = {
    //UIString(captionID + ".tooltip") += property.get().textProperty()
  }

  /**
   * Make sure the approriate code is run on the `FX`-thread.
   *
   * If we are already on the `FX`-thread, the code is run immediately, otherwise it is invoked later.
   */
  def onFX(action: Runnable): Unit =
    if (Platform.isFxApplicationThread)
      action.run()
    else
      Platform.runLater(action)
}
