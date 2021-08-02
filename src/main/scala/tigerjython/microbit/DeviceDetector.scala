/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.microbit

import tigerjython.utils.{OSPlatform, OSProcess}

/**
 * @author Tobias Kohn
 */
object DeviceDetector {

  private var _calliopePath: String = _
  private var _microBitPath: String = _

  def getCalliopePath: String = _calliopePath

  def getMicrobitPath: String = _microBitPath

  def hasCalliopeReady: Boolean =
    OSPlatform.system match {
      case OSPlatform.WINDOWS =>
        detectMicrobitWindows()
        _calliopePath != null
      case OSPlatform.LINUX | OSPlatform.MAC_OS =>
        detectMicrobitPosix()
        _calliopePath != null
      case _ =>
        _calliopePath = null
        false
    }

  def hasMicrobitReady: Boolean =
    OSPlatform.system match {
      case OSPlatform.WINDOWS =>
        detectMicrobitWindows()
      case OSPlatform.LINUX | OSPlatform.MAC_OS =>
        detectMicrobitPosix()
      case _ =>
        _microBitPath = null
        false
    }

  /**
   * Output of `mount`:
   *   fs_spec on fs_file type fs_vfstype (fs_mntopts)
   * We want the `fs_file` to check for `MICROBIT`.
   * (Source: https://unix.stackexchange.com/questions/91960/can-anyone-explain-the-output-of-mount)
   */
  private def detectMicrobitPosix(): Boolean = {
    _calliopePath = null
    _microBitPath = null
    val mountProcess = new OSProcess("mount")
    mountProcess.exec()
    for (line <- mountProcess.waitForOutput().split('\n')) {
      val elements = line.split(' ')
      if (elements.length >= 3 && elements(1) == "on") {
        val fs_file = elements(2)
        if (fs_file.endsWith("MICROBIT") && _microBitPath == null)
          _microBitPath = fs_file
        if (fs_file.endsWith("MINI") && _calliopePath == null)
          _calliopePath = fs_file
      }
    }
    _microBitPath != null
  }

  private def detectMicrobitWindows(): Boolean = {
    _calliopePath = null
    _microBitPath = null
    val wmiProcess = new OSProcess("wmic")
    wmiProcess.exec("logicaldisk", "where", "drivetype=2", "get", "description,deviceid,volumename")
    val lines = wmiProcess.waitForOutput().split('\n')
    if (lines.nonEmpty) {
      val lineIter = lines.iterator
      // Get the width of the individual columns
      val headers = lineIter.next().toLowerCase
      val startIndices = new Array[Int](3)
      startIndices(0) = headers.indexOf("description")
      startIndices(1) = headers.indexOf("deviceid")
      startIndices(2) = headers.indexOf("volumename")
      for (line <- lineIter) {
        if (line.length > startIndices(2)) {
          val volumeId = line.substring(startIndices(1), startIndices(2)).trim
          val volumeName = line.substring(startIndices(2)).trim
          if (volumeName.endsWith("MICROBIT") && _microBitPath == null)
            _microBitPath = volumeId
          if (volumeName.endsWith("MINI") && _calliopePath == null)
            _calliopePath = volumeId
          println(s"Found volume: `$volumeName` on `$volumeId`")
        } else
        if (line.nonEmpty)
          println(s"Found name-less volume: $line")
      }
    } else
      println("Have not found any volumes")
    _microBitPath != null
  }
}
