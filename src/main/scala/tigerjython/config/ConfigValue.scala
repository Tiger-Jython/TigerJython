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
