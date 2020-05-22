/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.core

import java.net.URI

import tigerjython.config.Parser

/**
 * In contrast to the `Preferences`, which are basically entirely user-controlled, the configurations are rather
 * settings determined by the system/application itself.  This includes, e.g., available languages.
 *
 * One tell-tale sign for the difference between configuration and preferences is that preferences are stored to be
 * persistent, whereas configurations are read-only and never written or changed by the application itself.
 *
 * @author Tobias Kohn
 */
object Configuration {

  private lazy val _availableLanguages: Seq[(String, String)] = {
    val languageMap = Parser.parseResource("languages.txt")
    val result = languageMap.map(pair => (pair._1, pair._2.value))
    (
      if (!result.contains("en"))
        result.toSeq :+ ("en" -> "English")
      else
        result.toSeq
    ).sortBy(_._1)
  }

  /**
   * Returns a list with all available languages as tuples of the form `(abbreviation, human-readable-form)`.
   */
  def availableLanguages: Seq[(String, String)] = _availableLanguages

  /**
   * Returns the version of the JRE the program is running on.  This only returns the major version.
   */
  lazy val getJavaVersion: Int = {
    val versionStr = System.getProperty("java.version")
    val version =
      if (versionStr.startsWith("1."))
        versionStr.drop(2).takeWhile(_.isDigit)
      else
        versionStr.takeWhile(_.isDigit)
    version.toInt
  }

  /**
   * Returns the two-character language code given a display-name.  For instance, `"English"` is turned into
   * `"en"`.  If the language cannot be found, `"en"` is returned as default.
   */
  def getLanguageCode(language: String): String =
    _availableLanguages.find(x => x._1 == language || x._2 == language) match {
      case Some((code, _)) =>
        code
      case None =>
        "en"
    }

  /**
   * This is the "source" of the application, i.e. the full path of the JAR-file.
   */
  var sourcePath: URI = _

  /**
   * Load the configurations from the respective files and resources.
   */
  def initialize(): Unit = {
    sourcePath = getClass.getProtectionDomain.getCodeSource.getLocation.toURI
  }
}
