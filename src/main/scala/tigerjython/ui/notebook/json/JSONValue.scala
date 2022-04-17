/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.notebook.json

sealed abstract class JSONValue {

  /**
   * In our application we are mostly interested in some specific elements in a JSON.  We therefore provide a method
   * to directly access an element given its path.
   *
   * Element access follows Python's syntax and convention.  However, note that slices can only be used as the last
   * bit of the path, i.e. after using a slice you cannot have another field access.
   *
   * Example: `jsonElement("cells[3].output[:-1]")`
   */
  def apply(path: String): Option[JSONValue] =
    if (path.isEmpty)
      Some(this)
    else
      None

  def asString: String = null

  def asString(path: String): String =
    apply(path) match {
      case Some(value) =>
        value.asString
      case _ =>
        null
    }
}

case class JSONBoolean(value: Boolean) extends JSONValue
case class JSONInteger(value: Int) extends JSONValue
case class JSONName(value: String) extends JSONValue {
  override def asString: String = value
}
case class JSONNumber(value: Double) extends JSONValue
case class JSONString(value: String) extends JSONValue {
  override def asString: String = value
}

case class JSONArray(items: Array[JSONValue]) extends JSONValue {
  override def apply(path: String): Option[JSONValue] =
    if (path.startsWith("[")) {
      val slice = path.drop(1).takeWhile(_ != ']')
      if (slice.contains(':')) {
        if (slice.length + 2 == path.length) {
          val startStr = slice.takeWhile(_ != ':')
          val stopStr = slice.drop(startStr.length + 1)
          val start =
            if (startStr != "") {
              val i = startStr.toInt
              if (i < 0)
                i + items.length
              else
                i
            } else
              0
          val stop =
            if (stopStr != "") {
              val i = stopStr.toInt
              if (i < 0)
                i + items.length
              else
                i
            } else
              items.length
          if (0 <= start && start <= stop && stop <= items.length)
            Some(JSONArray(items.slice(start, stop)))
          else
            None
        } else
          None
      } else {
        var i = slice.toInt
        if (i < 0)
          i += items.length
        if (0 <= i && i < items.length) {
          val rest = path.drop(slice.length + 2)
          if (rest.nonEmpty && rest(0) == '.')
            items(i)(rest.drop(1))
          else
            items(i)(rest)
        } else
          None
      }
    } else
      super.apply(path)

  override def asString: String = {
    val lines = collection.mutable.ArrayBuffer[String]()
    for (item <- items) {
      val s = item.asString
      if (s == null)
        return null
      lines += s
    }
    lines.mkString("")
  }
}
case class JSONMap(items: Map[String, JSONValue]) extends JSONValue {
  override def apply(path: String): Option[JSONValue] =
    if (path.nonEmpty) {
      val name = path.takeWhile(c => c != '.' && c != '[')
      items.get(name) match {
        case Some(value) =>
          value(path.drop(name.length + 1))
        case _ =>
          None
      }
    } else
      Some(this)
}
