/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.preferences

import java.lang

import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.Node
import javafx.scene.control.{ComboBox, Label, Spinner, SpinnerValueFactory}
import javafx.scene.layout.{StackPane, VBox}
import javafx.scene.text._
import tigerjython.core.{Configuration, Preferences}
import tigerjython.ui.UIString

/**
 * The preference settings for the general appearance; hence one of the key preference panes.
 *
 * @author Tobias Kohn
 */
class AppearancePreferencesPane extends PreferencePane {

  val caption: StringProperty = new SimpleStringProperty("Appearance")
  UIString("prefs.appearance") += caption

  // Testing of mono-spaced fonts as suggested in: https://yo-dave.com/2015/07/27/finding-mono-spaced-fonts-in-javafx/
  protected lazy val availableFonts: Array[String] = {
    val result = collection.mutable.ArrayBuffer[String](
      "monospace"
    )
    val textA = new Text("il  .")
    val textB = new Text("MNXPW")
    javafx.scene.text.Font.getFamilies.forEach({
      family =>
        val font = Font.font(family, FontWeight.NORMAL, FontPosture.REGULAR, 14.0)
        textA.setFont(font)
        textB.setFont(font)
        if (textA.getLayoutBounds.getWidth == textB.getLayoutBounds.getWidth)
          result += family
    })
    result.toArray
  }

  protected def createBasicProperties(): Seq[Node] = {
    val label = new Label("Tab width:")
    val tabWidthFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 12)
    val tabWidthSpinner = new Spinner(tabWidthFactory)
    tabWidthFactory.setValue(Preferences.tabWidth.get)
    tabWidthFactory.valueProperty().addListener(new ChangeListener[Integer] {
      override def changed(observableValue: ObservableValue[_ <: Integer], oldValue: Integer, newValue: Integer): Unit = {
        Preferences.tabWidth.setValue(newValue)
      }
    })
    label.setLabelFor(tabWidthSpinner)
    Seq(label, tabWidthSpinner)
  }

  protected def createFontSelection(): Seq[Node] = {
    val fontLabel = new Label("Font:")
    val fontChooser = new ComboBox[String]()
    fontLabel.setLabelFor(fontChooser)
    fontChooser.getItems.addAll(availableFonts: _*)
    fontChooser.getSelectionModel.select(Preferences.fontFamily.get)
    fontChooser.valueProperty().addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
        Preferences.fontFamily.setValue(newValue)
      }
    })
    val sizeLabel = new Label("Size:")
    val sizeFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(6.0, 64.0)
    val sizeChooser = new Spinner(sizeFactory)
    sizeFactory.setAmountToStepBy(1.0)
    sizeFactory.setValue(Preferences.fontSize.get)
    sizeFactory.valueProperty().addListener(new ChangeListener[lang.Double] {
      override def changed(observableValue: ObservableValue[_ <: lang.Double], oldValue: lang.Double, newValue: lang.Double): Unit = {
        Preferences.fontSize.setValue(newValue)
      }
    })
    sizeLabel.setLabelFor(sizeChooser)
    Seq(fontLabel, fontChooser, sizeLabel, sizeChooser)
  }

  protected def createThemes(): Seq[Node] = {
    val label = new Label("Theme:")
    val themeChooser = new ComboBox[String]()
    themeChooser.getItems.addAll(
      Configuration.availableThemes.map(_._2): _*
    )
    themeChooser.getSelectionModel.select(
      Configuration.availableThemes.indexWhere(_._1 == Preferences.theme.get)
    )
    label.setLabelFor(themeChooser)
    themeChooser.valueProperty().addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
        Configuration.availableThemes.find(_._2 == newValue) match {
          case Some((value, _)) =>
            Preferences.theme.setValue(value)
          case _ =>
        }
      }
    })
    Seq(label, themeChooser)
  }

  override lazy val node: Node = {
    val result = new VBox()
    result.getChildren.addAll(createBasicProperties(): _*)
    result.getChildren.addAll(createFontSelection(): _*)
    result.getChildren.addAll(createThemes(): _*)
    new StackPane(result)
  }
}
