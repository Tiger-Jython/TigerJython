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
class MicrobitFlasher(val controller: ExecutionController) extends MicroDeviceFlasher {

  private val fileSystem_v1 = new MicrobitFileSystem(1)
  private val fileSystem_v2 = new MicrobitFileSystem(2)

  def addTextFile(name: String, data: String): Unit = {
    fileSystem_v1.addTextFile(name, data)
    fileSystem_v2.addTextFile(name, data)
  }

  def addTextFile(name: String, data: String, version: Int): Unit = {
    if (version == 1)
      fileSystem_v1.addTextFile(name, data)
    else if (version == 2)
      fileSystem_v2.addTextFile(name, data)
  }

  def createHexFile: String = {
    if (fileSystem_v2.isOutOfMemory) {
      controller.appendToErrorOutput("not enough disk space on the Micro:bit (V2)")
      return null
    }
    else if (fileSystem_v1.isOutOfMemory) {
      controller.appendToErrorOutput("not enough disk space on the Micro:bit (V1)")
      return null
    }
    val hexMaker = new HexMaker()
    hexMaker.beginMicrobitV1Section()
    hexMaker.addRawResource("microPythonV1")
    fileSystem_v1.writeToHex(hexMaker)
    hexMaker.addRawResource("microPythonV1_UICR")
    hexMaker.endSection()
    hexMaker.beginMicrobitV2Section()
    hexMaker.addRawResource("microPythonV2")
    fileSystem_v2.writeToHex(hexMaker)
    hexMaker.addRawResource("microPythonV2b")
    hexMaker.addRawResource("microPythonV2_UICR")
    hexMaker.endSection(true)
    hexMaker.output
  }

  def writeToDevice(path: String = null): Unit =
    if (path != null)
      writeToDevice(new File(path))
    else if (DeviceDetector.hasMicrobitReady) {
      writeToDevice(new File(DeviceDetector.getMicrobitPath, "tigerjython.hex"))
    } else
      controller.appendToErrorOutput("no micro:bit device detected")
}
