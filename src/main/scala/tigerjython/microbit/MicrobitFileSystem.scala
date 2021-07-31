/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.microbit

import java.nio.charset.StandardCharsets

/**
 * Most of this code is adapted from Nicholas Tollervey's uFlash:
 * https://github.com/ntoll/uflash
 *
 * @author Tobias Kohn
 */
class MicrobitFileSystem(val microbitVersion: Int) {

  private val chunks = collection.mutable.ArrayBuffer[MicrobitFileSystemChunk]()

  protected val (fs_start_address, fs_end_address): (Int, Int) =
    microbitVersion match {
      case 1 =>
        (0x38C00, 0x3F800)
      case 2 =>
        (0x6D000, 0x72000)
    }
  protected val fs_size: Int = fs_end_address - fs_start_address

  private[microbit] def _addChunk(chunk: MicrobitFileSystemChunk): Int = {
    chunks += chunk
    chunks.length
  }

  protected def newChunk(): MicrobitFileSystemChunk =
    new MicrobitFileSystemChunk(this)

  def addBinaryFile(name: String, data: Array[Byte]): Unit =
    if (name != null && name.nonEmpty) {
      val chunk = newChunk()
      chunk.writeFilename(name)
      chunk.writeAll(data)
    }

  def addTextFile(name: String, data: String): Unit =
    if (name == null)
      addTextFile("main.py", data)
    else if (data.contains('\r'))
      addTextFile(name, data.filter(_ != '\r'))
    else if (name.nonEmpty && data.nonEmpty)
      addBinaryFile(name, data.getBytes(StandardCharsets.UTF_8))

  def available: Int = {
    val avail = (fs_size - size) max 0
    val chunkCount = avail / 128
    chunkCount * 126
  }

  def isOutOfMemory: Boolean = size > fs_size

  def size: Int = chunks.length * 128

  protected[microbit] def writeToHex(hexMaker: HexMaker): Unit = {
    if (size > fs_size)
      throw new RuntimeException("microbit file system exceeding maximum size")
    hexMaker.setAddress(fs_start_address)
    for (chunk <- chunks)
      chunk.writeToHex(hexMaker)
    /*hexMaker.setAddress(fs_end_address)
    hexMaker.addBinary(Array(0xFD.toByte))*/
  }
}
