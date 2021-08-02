/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.execute

import tigerjython.microbit._
import tigerjython.ui.Dialogs

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
    flasher.addTextFile("main.py", controller.getExecutableFileAsString)
    for ((name, text) <- controller.getRequiredModules(deviceName, ExecLanguage.PYTHON_3))
      flasher.addTextFile(name + ".py", text)
    controller.clearOutput()
    if (flasher.getDevicePath == null)
      Dialogs.selectMicroDeviceTarget(deviceName) match {
        case Some(path) =>
          controller.appendToLog("Target path: '%s'".format(path))
          flasher.writeToDevice(path)
          controller.appendToLog("Download completed")
        case _ =>
      }
    else {
      controller.appendToLog("Target path: '%s'".format(flasher.getDevicePath))
      flasher.writeToDevice()
      controller.appendToLog("Download completed")
    }
  }

  override def shutdown(): Unit = {}

  def stop(): Unit = {}

  def writeToInput(ch: Char): Unit = {}

  def writeToInput(s: String): Unit = {}
}
