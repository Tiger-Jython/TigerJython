/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.microbit

import java.io.{File, FileWriter}

import tigerjython.execute.ExecutionController

/**
 * @author Tobias Kohn
 */
abstract class MicroDeviceFlasher {

  def controller: ExecutionController

  def addTextFile(name: String, data: String): Unit

  def createHexFile: String

  def writeToDevice(path: String = null): Unit

  def writeToDevice(path: File): Unit = {
    controller.appendToLog("Writing hex file to path: " + path.getAbsoluteFile)
    val fw = new FileWriter(path)
    try {
      val hexFile = createHexFile
      if (hexFile != null)
        fw.write(hexFile)
      else
        controller.appendToErrorOutput("unable to create hex file")
    } finally {
      fw.flush()
      fw.close()
    }
  }
}
