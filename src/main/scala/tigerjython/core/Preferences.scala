/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.core

import java.util.{Base64, Locale, Random}
import java.util.prefs.{Preferences => JPreferences}

import javafx.beans.property
import javafx.beans.property._
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.text.{Font, Text}
import tigerjython.utils._

/**
 * The global preferences keep track of values used to customise the application.  By binding to the respective
 * properties, you can make sure that the values are directly stored persistently, and can thus be restored the
 * next time the application runs.
 *
 * A typical scheme might be:
 * ```
 * setPrefXValue(Preferences.xValue.get())
 * Preferences.xValue.bind(XValueProperty)
 * ```
 * or, where possible, you can directly use:
 * ```
 * XValueProperty.bindBidirectional(Preferences.xValue)
 * ```
 *
 * There are additional configurations controlled by, e.g., files included in the distribution, such as the set of
 * available languages.  Those can be found in the `Configuration` object.
 *
 * @author Tobias Kohn
 */
object Preferences {

  protected val preferences: JPreferences = JPreferences.userNodeForPackage(getClass)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  // Private methods used to obtain some system standard values

  lazy val getDefaultFontSize: Double =
    try {
      new Text().getFont.getSize
    } catch {
      case _: Throwable =>
        12.0
    }

  private def getDefaultLanguage: String =
    Locale.getDefault.getLanguage

  private def getDefaultTheme: String = "tigerjython"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  // UI Preferences

  val defaultFont: SimpleObjectProperty[Font] = new SimpleObjectProperty[Font](new Font(getDefaultFontSize))

  val fontFamily: StringProperty = new PrefStringProperty(preferences, "editor.font-family", "monospace")

  val fontSize: DoubleProperty = new PrefDoubleProperty(preferences, "editor.font-size", getDefaultFontSize)

  val globalZoom: DoubleProperty = new PrefDoubleProperty(preferences, "global.zoom", 1.0)

  val language: StringProperty = new PrefStringProperty(preferences, "language", getDefaultLanguage)

  val languageCode: StringProperty = new SimpleStringProperty()

  val tabWidth: IntegerProperty = new PrefIntegerProperty(preferences, "tabWidth", 4)

  val theme: StringProperty = new PrefStringProperty(preferences, "editor.theme", getDefaultTheme)

  val windowHeight: DoubleProperty = new PrefDoubleProperty(preferences, "window.height", 600)

  val windowWidth: DoubleProperty = new PrefDoubleProperty(preferences, "window.width", 800)

  {
    fontSize.addListener(new ChangeListener[Number] {
      override def changed(observableValue: ObservableValue[_ <: Number], t: Number, t1: Number): Unit = {
        updateDefaultFont()
      }
    })
    globalZoom.addListener(new ChangeListener[Number] {
      override def changed(observableValue: ObservableValue[_ <: Number], t: Number, t1: Number): Unit = {
        updateDefaultFont()
      }
    })
    updateDefaultFont()
  }

  private def updateDefaultFont(): Unit = {
    val f = fontSize.get() * globalZoom.get()
    if (f != defaultFont.get().getSize) {
      defaultFont.get.getName
      defaultFont.setValue(new Font(f))
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  {
    val code = Configuration.getLanguageCode(language.get)
    languageCode.setValue(code)
    if (preferences.get("user-number", null) == null)
      preferences.put("user-number", generateUserNumber)
  }

  language.addListener(new ChangeListener[String] {
    override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
      val code = Configuration.getLanguageCode(newValue)
      languageCode.setValue(code)
    }
  })

  /**
   * Creates a new quasi-unique random string as a pseudo-user-id.  This is only created the first time TigerJython is
   * run on a specific machine and cannot be modified afterwards.  This user-number is used for purposes of research,
   * where you might want to collect a user's edits anonymously (without revealing the actual identity).  However, the
   * user-number here is generated irrespective of whether it is actually used or not.  It might just sit there dormant
   * in the preferences, making sure that opting in or out of such research programmes does not generate a new number.
   *
   * The generated user number is a modified base64 encoded ASCII string, representing (pseudo-)random bytes.  By
   * replacing `/` by `-`, it can also be used as a file-name.  Furthermore note that each string starts with the
   * same letter.  This allows to change the scheme of the user numbers to be changed later on, where the initial
   * letter will be modified accordingly, allowing a server to easily detect the used scheme.
   *
   * Finally note that there is, in principle, a chance that more than one user might have the same user numbers, but
   * since it is used merely to differentiate between users taking part in a study and not for actual identification,
   * this risk is acceptable.
   */
  private def generateUserNumber: String = {
    val data = new Array[Byte](48)
    new Random().nextBytes(data)
    val result = "A" + Base64.getEncoder.encodeToString(data).replace('/', '-')
    println(result)
    result
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  // General and Python-Related Preferences

  val checkSyntax: BooleanProperty = new PrefBooleanProperty(preferences, "syntaxcheck", true)

  val checkUpdates: BooleanProperty = new PrefBooleanProperty(preferences, "check-updates", true)

  val pythonInterpreter: StringProperty = new PrefStringProperty(preferences, "python.interpreter")

  val repeatLoop: BooleanProperty = new PrefBooleanProperty(preferences, "repeat-loop", false)

  val sendStatistics: BooleanProperty = new PrefBooleanProperty(preferences, "send-statistics")

  val syntaxCheckIsStrict: BooleanProperty =
    new PrefBooleanProperty(preferences, "syntaxcheck-strict", true)

  val syntaxCheckRejectDeadCode: BooleanProperty =
    new PrefBooleanProperty(preferences, "syntaxcheck-deadcode", false)

  val userNumber: ReadOnlyStringProperty = new PrefStringProperty(preferences, "user-number")

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
