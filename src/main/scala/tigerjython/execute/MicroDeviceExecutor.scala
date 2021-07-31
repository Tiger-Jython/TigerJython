/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

import tigerjython.microbit._

/**
 * @author Tobias Kohn
 */
class MicroDeviceExecutor(val controller: ExecutionController, val deviceName: String) extends Executor {

  protected val flasher: MicroDeviceFlasher =
    deviceName match {
      case "Microbit" | "Micro:bit" =>
        new MicrobitFlasher(controller)
      case "Calliope" | "Calliope Mini" =>
        new CalliopeFlasher(controller)
    }

  def run(): Unit = {
    val filename = controller.getExecutableFile.getAbsolutePath
    controller.appendToLog("Downloading '%s' to '%s'".format(filename, deviceName))
    flasher.addTextFile("main.py", controller.getText)
    controller.clearOutput()
    flasher.writeToDevice()
    controller.appendToLog("Download complete")
  }

  override def shutdown(): Unit = {}

  def stop(): Unit = {}

  def writeToInput(ch: Char): Unit = {}

  def writeToInput(s: String): Unit = {}
}
