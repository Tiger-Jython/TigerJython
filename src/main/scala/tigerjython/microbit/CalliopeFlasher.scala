/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.microbit

import java.io.File

import tigerjython.execute.ExecutionController

/**
 * @author Tobias Kohn
 */
class CalliopeFlasher(val controller: ExecutionController) extends MicroDeviceFlasher {

  private val fileSystem = new MicrobitFileSystem(1)

  def addTextFile(name: String, data: String): Unit = {
    fileSystem.addTextFile(name, data)
  }

  def createHexFile: String = {
    if (fileSystem.isOutOfMemory) {
      controller.appendToErrorOutput("not enough disk space on the Calliope Mini")
      return null
    }
    val hexMaker = new HexMaker()
    hexMaker.addRawResource("microPythonV1")
    fileSystem.writeToHex(hexMaker)
    hexMaker.addRawResource("microPythonV1_UICR")
    hexMaker.endFile()
    hexMaker.output
  }

  def writeToDevice(path: String = null): Unit =
    if (path != null)
      writeToDevice(new File(path))
    else if (DeviceDetector.hasCalliopeReady) {
      writeToDevice(new File(DeviceDetector.getCalliopePath, "tigerjython.hex"))
    } else
      controller.appendToErrorOutput("no Calliope Mini device detected")
}
