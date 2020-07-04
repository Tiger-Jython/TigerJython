/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.editing

import java.nio.ByteBuffer

import org.fxmisc.richtext.model.PlainTextChange
import tigerjython.utils.PrefTextUtils

/**
 * This is a helper object to encode and decode streams of Undo-events.
 *
 * @author Tobias Kohn
 */
object UndoHelper {

  // To limit the strain on resources, we put a maximum number of undo steps that are saved together with the file.
  // This number is fairly arbitrary and can be changed.  However, note that the maximum length of text that can be
  // stored in the preferences file is also limited (according to the Java docs to about 8 KB, but we play it a bit
  // save with 4KB instead).
  // This number cannot be larger than 512, because each item takes up 16 bytes and we cannot have more than
  // the 8 KB.
  private val MAX_UNDO_SAVE_COUNT = 41  // we fall just short of perfection here, of course ;-)

  private val MAX_TEXT_LENGTH = PrefTextUtils.MAX_STRING_LENGTH / 2

  def decode(indicesBuffer: Array[Byte], text: String): Array[PlainTextChange] = {
    val result = new Array[PlainTextChange](indicesBuffer.length / 16)
    val indices = ByteBuffer.wrap(indicesBuffer).asIntBuffer()
    for (i <- result.indices) {
      val pos = indices.get(4 * i)
      val txtPos = indices.get(4 * i+1)
      val insCount = indices.get(4 * i+2)
      val delCount = indices.get(4 * i+3)
      val inserted = text.substring(txtPos, txtPos + insCount)
      val removed = if (delCount > 0) text.substring(txtPos + insCount, txtPos + insCount + delCount) else ""
      result(i) = new PlainTextChange(pos, removed, inserted)
    }
    result
  }

  def encode(changes: Array[PlainTextChange]): (Array[Byte], String) = {
    val len = changes.length min MAX_UNDO_SAVE_COUNT
    val text: StringBuilder = new StringBuilder()
    val buf = ByteBuffer.allocate(16 * len)
    val buffer = buf.asIntBuffer()
    var i = 0
    while (text.length() < MAX_TEXT_LENGTH && i < len) {
      val item = changes(i)
      val insText = item.getInserted
      val delText = item.getRemoved
      buffer.put(item.getPosition)
      buffer.put(text.length)
      buffer.put(insText.length)
      buffer.put(delText.length)
      text ++= insText
      text ++= delText
      i += 1
    }
    (buf.array().take(16 * i), text.toString)
  }
}
