/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.preferences

import javafx.scene.Node
import javafx.scene.control.{ComboBox, Label, Spinner, SpinnerValueFactory}
import javafx.scene.layout.{StackPane, VBox}
import javafx.scene.text._

/**
 * The preference settings for the general appearance; hence one of the key preference panes.
 *
 * @author Tobias Kohn
 */
class AppearancePreferencesPane extends PreferencePane {

  override def caption: String = "Appearance"

  // Testing of mono-spaced fonts as suggested in: https://yo-dave.com/2015/07/27/finding-mono-spaced-fonts-in-javafx/
  protected lazy val availableFonts: Array[String] = {
    val result = collection.mutable.ArrayBuffer[String]()
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
    val tabWidth = new Spinner(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 12))
    label.setLabelFor(tabWidth)
    Seq(label, tabWidth)
  }

  protected def createFontSelection(): Seq[Node] = {
    val fontLabel = new Label("Font:")
    val fontChooser = new ComboBox[String]()
    fontLabel.setLabelFor(fontChooser)
    fontChooser.getItems.addAll(availableFonts: _*)
    val sizeLabel = new Label("Size:")
    val sizeChooser = new Spinner(new SpinnerValueFactory.IntegerSpinnerValueFactory(6, 64))
    sizeLabel.setLabelFor(sizeChooser)
    Seq(fontLabel, fontChooser, sizeLabel, sizeChooser)
  }

  protected def createThemes(): Seq[Node] = {
    Seq()
  }

  override lazy val node: Node = {
    val result = new VBox()
    result.getChildren.addAll(createBasicProperties(): _*)
    result.getChildren.addAll(createFontSelection(): _*)
    result.getChildren.addAll(createThemes(): _*)
    new StackPane(result)
  }
}
