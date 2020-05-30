/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui

import javafx.application.Platform
import javafx.beans.property.StringProperty
import tigerjython.configparser.{ConfigValue, Parser}

/**
 * A `UIString` holds a string value that depends on the user's locale and language.  It is loaded at runtime from an
 * external source and can be changed if the user selects different language settings.
 *
 * We automatically keep track of all `UIString`-instances.  When the language/locale settings have changed, call
 * `invalidateAll()` or `setProvider(...)` to reload all strings.
 *
 * @author Tobias Kohn
 */
class UIString private (val name: String) {

  private var _properties: collection.mutable.Set[StringProperty] = collection.mutable.Set[StringProperty]()
  private var _value: String = _

  def apply(): String = value

  private def load(): Unit = synchronized {
    if (UIString.provider == null) {
      _value = "?<%s>".format(name)
      return
    }
    _value = UIString.provider.getUIString(name)
    if (_properties != null)
      Platform.runLater(() => {
        for (n <- _properties)
          n.setValue(_value)
      })
  }

  def value: String = {
    if (_value == null)
      load()
    if (_value != null)
      _value
    else
      "<%s>".format(name)
  }

  def +=(property: StringProperty): Unit =
    if (property != null) {
      _properties += property
      property.setValue(value)
    }

  def -=(property: StringProperty): Unit =
    _properties -= property
}
object UIString {

  /**
   * A `StringProvider` is a map-like instance responsible for resolving names to actual user interface texts.
   */
  trait StringProvider {

    def getUIString(name: String): String

  }

  object StringProvider {

    class MapStringProvider(val source: Map[String, String]) extends StringProvider {

      override def getUIString(name: String): String = source.getOrElse(name, "")
    }

    def fromMap(source: Map[String, ConfigValue]): StringProvider =
      new MapStringProvider(
        source.map(item => (item._1, item._2.asString.filter(_ != '&')))
      )
  }

  private var provider: StringProvider = _
  private val registry: collection.mutable.Map[String, UIString] = collection.mutable.Map[String, UIString]()

  /**
   *
   * @param name  The name that identifies the text value to load.
   * @return      A `UIString`-instance.
   */
  def apply(name: String): UIString = {
    assert(name != null)
    registry.getOrElseUpdate(name, new UIString(name))
  }

  /**
   *
   * @param name  The name that identifies the text value to load.
   * @return      A `UIString` instance, if it already exists, or `None` otherwise.
   */
  def get(name: String): Option[UIString] = {
    assert(name != null)
    registry.get(name)
  }

  /**
   * Load the texts for the user interface for the given language.  This will look for the file
   * `resources/uitexts_XX.txt`, where `XX` stands for the language.
   */
  def loadFromResource(language: String): Unit = {
    val resMap = Parser.parseResource("uitexts_%s.txt".format(language))
    val provider = StringProvider.fromMap(resMap)
    setProvider(provider)
  }

  /**
   * Reload all strings in the user interface.
   */
  def invalidateAll(): Unit =
    if (provider != null) {
      for ((_, item) <- registry)
        item.load()
    }

  /**
   * Set a new provider for the user interface strings, i.e. to set the user interface language.
   *
   * @param stringProvider  The new string-provider, must not be `null`.
   */
  def setProvider(stringProvider: StringProvider): Unit =
    if (stringProvider != null) {
      provider = stringProvider
      invalidateAll()
    }
}