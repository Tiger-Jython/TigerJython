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
 * @author Tobias Kohn
 */
class MicrobitFileSystemChunk(val parent: MicrobitFileSystem) {

  private val data: Array[Byte] = new Array[Byte](128)
  private var pos: Int = 0

  val index: Int = parent._addChunk(this)

  {
    for (i <- data.indices)
      data(i) = 0xFF.toByte
    data(0) = 0xFE.toByte
  }

  def available: Int = (data.length - 2) - pos

  def currentOffset: Int = pos

  def setEndOffset(offset: Int): Unit =
    data(1) = (offset & 0xFF).toByte

  def setNextIndex(index: Int): Unit =
    data(data.length - 1) = (index & 0xFF).toByte

  def setPrevIndex(index: Int): Unit =
    data(0) = (index & 0xFF).toByte

  def write(data: Array[Byte]): Int =
    write(data, 0)

  def write(data: Array[Byte], offset: Int): Int =
    if (data.nonEmpty && offset < data.length && available > 0) {
      val len = (data.length - offset) min available
      Array.copy(data, offset, this.data, pos + 1, len)
      pos += len
      len
    } else
      0

  def write(text: String): Int =
    if (text != null && text.nonEmpty)
      write(text.getBytes(StandardCharsets.UTF_8))
    else
      0

  def writeAll(data: Array[Byte]): Int =
    writeAll(data, 0)

  def writeAll(data: Array[Byte], offset: Int): Int =
    if (data.nonEmpty && data.length - offset >= available) {
      var index = offset + write(data, offset)
      var lastChunk = this
      while (index < data.length) {
        val chunk = new MicrobitFileSystemChunk(parent)
        val len = chunk.write(data, index)
        index += len
        lastChunk.setNextIndex(chunk.index)
        chunk.setPrevIndex(lastChunk.index)
        lastChunk = chunk
      }
      setEndOffset(lastChunk.currentOffset)
      index - offset
    } else {
      val len = write(data)
      setEndOffset(currentOffset)
      len
    }

  def writeFilename(filename: String): Int = {
    val bytes = filename.getBytes(StandardCharsets.UTF_8)
    data(2) = bytes.length.toByte
    pos = 2
    write(bytes)
    pos
  }

  protected[microbit] def writeToHex(hexMaker: HexMaker): Unit =
    hexMaker.addBinary(data)
}
