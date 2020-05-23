/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.core

import java.lang
import java.util.Locale
import java.util.prefs.{Preferences => JPreferences}

import javafx.beans.property._
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.text.Text

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

  // We define our own flavour of preferences, each with the ability to interact with the persistent storage.

  protected class PrefBooleanProperty(val name: String, default: Boolean = false) extends
    SimpleBooleanProperty(preferences.getBoolean(name, default)) {

    addListener(new ChangeListener[lang.Boolean] {
      override def changed(observableValue: ObservableValue[_ <: lang.Boolean], oldValue: lang.Boolean,
                           newValue: lang.Boolean): Unit =
        preferences.putBoolean(name, newValue)
    })
  }

  protected class PrefDoubleProperty(val name: String, default: Double = 0.0) extends
    SimpleDoubleProperty(preferences.getDouble(name, default)) {

    addListener(new ChangeListener[Number] {
      override def changed(observableValue: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit =
        preferences.putDouble(name, newValue.doubleValue())
    })
  }

  protected class PrefIntegerProperty(val name: String, default: Int = 0) extends
    SimpleIntegerProperty(preferences.getInt(name, default)) {

    addListener(new ChangeListener[Number] {
      override def changed(observableValue: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit =
        preferences.putInt(name, newValue.intValue())
    })
  }

  protected class PrefStringProperty(val name: String, default: String = null) extends
    SimpleStringProperty(preferences.get(name, default)) {

    addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit =
        preferences.put(name, newValue)
    })
  }

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

  val fontFamily: StringProperty = new PrefStringProperty("editor.font-family", "monospace")

  val fontSize: DoubleProperty = new PrefDoubleProperty("editor.font-size", getDefaultFontSize)

  val globalZoom: DoubleProperty = new PrefDoubleProperty("global.zoom", 1.0)

  val language: StringProperty = new PrefStringProperty("language", getDefaultLanguage)

  val languageCode: StringProperty = new SimpleStringProperty()

  val tabWidth: IntegerProperty = new PrefIntegerProperty("tabWidth", 4)

  val theme: StringProperty = new PrefStringProperty("editor.theme", getDefaultTheme)

  val windowHeight: DoubleProperty = new PrefDoubleProperty("window.height", 600)

  val windowWidth: DoubleProperty = new PrefDoubleProperty("window.width", 800)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  {
    val code = Configuration.getLanguageCode(language.get)
    languageCode.setValue(code)
  }

  language.addListener(new ChangeListener[String] {
    override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
      val code = Configuration.getLanguageCode(newValue)
      languageCode.setValue(code)
    }
  })

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  // General and Python-Related Preferences

  val checkUpdates: BooleanProperty = new PrefBooleanProperty("check-updates", true)

  val pythonInterpreter: StringProperty = new PrefStringProperty("python.interpreter")

  val repeatLoop: BooleanProperty = new PrefBooleanProperty("repeat-loop", false)

  val sendStatistics: BooleanProperty = new PrefBooleanProperty("send-statistics")

  val syntaxCheckIsStrict: BooleanProperty = new PrefBooleanProperty("syntaxcheck-strict", true)

  val syntaxCheckRejectDeadCode: BooleanProperty = new PrefBooleanProperty("syntaxcheck-deadcode", false)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
