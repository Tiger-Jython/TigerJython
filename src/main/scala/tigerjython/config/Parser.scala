/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.config

/**
 * The parser takes a text as input and returns a map that maps qualified identifiers to values.
 *
 * The idea of the configuration files is to support as wide a range of possible input formats as possible, including
 * CSS-ish, JSON, or simple assignment maps.  That is, the configuration file could look like:
 * ```
 * menu {
 *     file: 'File';
 *     edit: 'Edit';
 * }
 * ### OR: ###
 * menu.file = "File"
 * menu.edit = "Edit"
 * ```
 * In either case, the returned map will contain the two keys `"menu.file"` and `"menu.edit"`, i.e. the map is flat.
 *
 * @author Tobias Kohn
 */
class Parser(val source: TokenSource) {

  private val definitions = collection.mutable.Map[String, ConfigValue]()

  private var nameStack: List[String] = Nil

  /**
   * Adds a definition for the given name.
   */
  def addDefinition(name: String, value: String): Unit =
    if (name != null && name != "")
      definitions(name.toLowerCase) = ConfigValue(value)

  /**
   * Returns a map that maps names to `ConfigValue`s.
   */
  def getDefinitions: Map[String, ConfigValue] =
    definitions.toMap

  protected def getPrefixName: String =
    if (nameStack != null && nameStack.nonEmpty)
      nameStack.head
    else
      ""

  protected def getValueOfName(name: String): String =
    definitions.get(name.toLowerCase) match {
      case Some(ConfigValue(value)) =>
        value
      case _ =>
        for (prefix <- nameStack)
          definitions.get((prefix + name).toLowerCase()) match {
            case Some(ConfigValue(value)) =>
              return value
            case _ =>
          }
        ""
    }

  protected def popName(): Unit =
    if (nameStack.nonEmpty)
      nameStack = nameStack.tail

  protected def pushName(name: String): Unit =
    if (name != null && name != "")
      nameStack = (getPrefixName + name + ".") :: nameStack

  /**
   * Parses the entire input and returns the map of definitions.
   */
  def parseAll(): Map[String, ConfigValue] = {
    while (source.hasNext)
      parseStatement()
    definitions.toMap
  }

  def parseName(): String =
    source.next() match {
      case null =>
        ""
      case Token.NAME(result) =>
        if (source.hasNext && source.peek() == Token.DOT) {
          source.next()
          result + "." + parseName()
        } else
          result
      case _ =>
        source.skipLine()
        parseName()
    }

  def parseStatement(): Unit =
    source.peek() match {
      case null =>
      case Token.RIGHT_BRACE =>
        source.next()
        popName()
      case Token.NAME(_) =>
        val name = parseName()
        source.next() match {
          case Token.ASSIGNMENT =>
            if (source.peek() == Token.LEFT_BRACE) {
              source.next()
              pushName(name)
            } else
              addDefinition(getPrefixName + name, parseValue())
          case Token.LEFT_BRACE =>
            pushName(name)
          case _ =>
        }
      case _ =>
        source.next()
    }

  def parseValue(): String = {
    val result =
      source.peek() match {
        case Token.NAME(_) =>
          getValueOfName(parseName())
        case Token.VALUE(s) =>
          source.next()
          s
        case _ =>
          return ""
      }
    if (source.peek() == Token.PLUS) {
      source.next()
      result + parseValue()
    } else
      result
  }
}
object Parser {

  def apply(tokenSource: TokenSource): Parser =
    new Parser(tokenSource)

  def apply(source: CharSequence): Parser =
    new Parser(new TokenSource(source))

  def apply(source: Iterator[String]): Parser =
    apply(source.mkString("\n"))

  def fromResource(name: String): Parser = {
    val res = getClass.getClassLoader.getResourceAsStream("resources/" + name)
    if (res != null) {
      val source = scala.io.Source.fromInputStream(res)("utf-8")
      tigerjython.config.Parser(source.getLines())
    } else
      null
  }

  def parse(source: CharSequence): Map[String, ConfigValue] =
    apply(source).parseAll()

  def parse(source: Iterator[String]): Map[String, ConfigValue] =
    apply(source).parseAll()

  def parseResource(name: String): Map[String, ConfigValue] =
    fromResource(name).parseAll()
}
