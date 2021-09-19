/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.microbit

import java.io.InputStream
import java.nio.charset.StandardCharsets

/**
 * Responsible for converting binary or string data to Intel HEX format.
 *
 * Most of this code is adapted from Nicholas Tollervey's uFlash:
 * https://github.com/ntoll/uflash
 *
 * @author Tobias Kohn
 */
class HexMaker(private val startAddress: Int) {

  private final val LINE_LENGTH = 0x10

  private var _addr: Int = 0
  private var _dataMarker: Byte = 0x00
  private val _output = new StringBuilder()

  {
    appendLinearAddress(startAddress >> 16)
    _addr = startAddress & 0xFFFF
  }

  def this() = this(0)

  protected def addByte(value: Int): Unit = {
    val s = Integer.toHexString(value).toUpperCase()
    if (s.length == 1) {
      _output += '0'
      _output += s(0)
    } else
      _output.append(s.takeRight(2))
  }

  protected def addBytes(values: Int*): Int = {
    var result = 0
    for (value <- values) {
      addByte(value)
      result += (value & 0xFF)
    }
    result
  }

  protected def addBytes(values: Array[Byte], len: Int): Int = {
    var result = 0
    for (i <- 0 until len) {
      val value = values(i)
      addByte(value)
      result += (value & 0xFF)
    }
    result
  }

  protected def addChar(ch: Char): Unit =
    _output += ch

  protected def addWord(value: Int): Int = {
    val high = (value >> 8) & 0xFF
    val low = value & 0xFF
    addByte(high)
    addByte(low)
    high + low
  }

  protected def appendBlockEnd(padding: Int = 0): Unit = {
    beginLine(padding)
    addBytes(0x00, 0x00, 0x0B)
    for (_ <- 0 until padding)
      addByte(0xFF)
    endLine(0x0B)
  }

  protected def appendFileEnd(): Unit = {
    beginLine(0x00)
    addBytes(0x00, 0x00, 0x01)
    endLine(1)
  }

  protected def appendBlockStart(blockType: Int): Unit = {
    beginLine(0x04)
    addBytes(0x00, 0x00, 0x0A)
    val sum = addWord(blockType)
    addBytes(0xC0, 0xDE)
    endLine(4 + sum + 0x0A + 0xC0 + 0xDE)
  }

  protected def appendDataLine(data: Array[Byte]): Unit =
    if (data.nonEmpty) {
      val addr = _addr & 0xFFFF
      val addrHigh = (addr >> 8) & 0xFF
      val addrLow  = addr & 0xFF
      val len = data.length min 0xFF
      val sum = len + addrHigh + addrLow + data.sum + _dataMarker
      beginLine(len)
      addBytes(addrHigh, addrLow, _dataMarker)
      addBytes(data, len)
      endLine(sum)
      _addr += len
      if (addr + len >= 0x10000)
        appendLinearAddress()
    }

  protected def appendLinearAddress(): Unit =
    if (_addr != startAddress || outputLength > 36)
      appendLinearAddress(_addr >> 16)

  protected def appendLinearAddress(a: Int): Unit = {
    val high = (a >> 8) & 0xFF
    val low = a & 0xFF
    beginLine(0x02)
    val sum = addBytes(0x00, 0x00, 0x04, high, low)
    endLine(2 + sum)
  }

  protected def appendPadLine(len: Int): Unit = {
    beginLine(len)
    addBytes(0x00, 0x00, 0x0C)
    for (_ <- 0 until len)
      addByte(0xFF)
    endLine(0x0C)
  }

  protected def beginLine(len: Int): Unit = {
    _output += ':'
    addByte(len)
  }

  protected def endLine(sum: Int): Unit = {
    addByte(-sum)
    _output += '\n'
  }

  def output: String = _output.toString()

  protected def outputLength: Int = _output.length()

  protected def remainingPageLength: Int = 0x200 - (outputLength & 0x1FF)

  def addBinary(data: Array[Byte]): Unit =
    if (data.nonEmpty) {
      var i = 0
      while (i < data.length) {
        appendDataLine(data.slice(i, i + LINE_LENGTH))
        i += LINE_LENGTH
      }
    }

  def addRawHex(data: String): Unit =
    if (data != null)
      _output.append(data)

  def addRawResource(fileName: String): Unit = {
    val stream = getClass.getClassLoader.getResourceAsStream("resources/%s.hex".format(fileName))
    if (stream != null) {
      var data = readAllBytes(stream)
//      var data = stream.readAllBytes()
      if (data.contains('\r'))
        data = data.filter(_ != '\r')
      while (data.length > 2 && data.last == '\n' && data(data.length - 2) == '\n')
        data = data.dropRight(1)
      if (data.nonEmpty) {
        _output.append(new String(data))
        if (data.last != '\n')
          _output += '\n'
      }
    }
  }

  // TODO: Properly test this method!
  // This method is required because `stream.readAllBytes` was introduced with Java 9.  However, it loosely
  // follows that code.
  private def readAllBytes(stream: InputStream): Array[Byte] = {
    val buffer = _readAllBytes(stream)
    if (buffer.isEmpty)
      return new Array[Byte](0)
    else if (buffer.length == 1)
      return buffer.head
    val len = buffer.map(_.length).sum
    val result = new Array[Byte](len)
    var i = 0
    for (item <- buffer) {
      Array.copy(item, 0, result, i, item.length)
      i += item.length
    }
    result
  }

  private def _readAllBytes(stream: InputStream): List[Array[Byte]] =
    if (stream.available() > 0) {
      val data = new Array[Byte](stream.available())
      val len = stream.read(data)
      if (len < data.length)
        data.take(len) :: Nil
      else
        data :: _readAllBytes(stream)
    } else {
      val buffer = new Array[Byte](1024)
      val n = stream.read(buffer)
      if (n == 0)
        Nil
      else if (n < buffer.length)
        buffer.take(n) :: Nil
      else
        buffer :: _readAllBytes(stream)
    }

  def addText(data: String): Unit =
    addBinary(data.getBytes(StandardCharsets.UTF_8))

  def beginMicrobitV1Section(): Unit = {
    _dataMarker = 0x00
    appendBlockStart(0x9901)
  }

  def beginMicrobitV2Section(): Unit = {
    _dataMarker = 0x0D
    _addr = startAddress
    appendLinearAddress()
    appendBlockStart(0x9903)
  }

  def endFile(): Unit =
    appendFileEnd()

  def endSection(endFile: Boolean = false): Unit =
    if (endFile) {
      if (remainingPageLength < 12) {
        for (_ <- 0 until remainingPageLength)
          _output += '\n'
        appendBlockEnd()
      } else if (remainingPageLength < 24)
        appendBlockEnd((remainingPageLength - 12) / 2)
      else {
        while (remainingPageLength > (24 + 2 * LINE_LENGTH))
          appendPadLine(LINE_LENGTH)
        if (remainingPageLength > 24)
          appendPadLine((remainingPageLength - 24) / 2)
        appendBlockEnd((remainingPageLength - 12) / 2)
      }
      appendFileEnd()
    } else {
      // Pad the current section to make sure it ends at a 512-bytes-boundary
      if (remainingPageLength < 12) {
        for (_ <- 0 until remainingPageLength)
          _output += '\n'
        appendLinearAddress()
      }
      while (remainingPageLength > (24 + 2 * LINE_LENGTH))
        appendPadLine(LINE_LENGTH)
      if (remainingPageLength > 24)
        appendPadLine((remainingPageLength - 24) / 2)
      appendBlockEnd((remainingPageLength - 12) / 2)
    }

  def setAddress(addr: Int): Unit = {
    _addr = addr
    appendLinearAddress()
  }
}
