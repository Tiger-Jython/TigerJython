/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.config

/**
 * The `ConfigValue` is a convenient way to read config values as a specific type.
 *
 * @author Tobias Kohn
 */
case class ConfigValue(val value: String) {

  def asBoolean: Boolean =
    value.toLowerCase match {
      case "true" | "t" | "yes" | "y" | "on" | "1" =>
        true
      case "false" | "f" | "no" | "n" | "off" | "0" =>
        false
      case _ =>
        false
    }

  def asFloat: Double =
    try {
      value.filter(_ > ' ').toDouble
    } catch {
      case _: NumberFormatException =>
        0.0
    }

  def asInteger: Int =
    try {
      value.filter(_ > ' ').toInt
    } catch {
      case _: NumberFormatException =>
        0
    }

  def asString: String = value

  override def toString: String = value
}
