/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.preferences

import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.Node
import javafx.scene.control.{ComboBox, Label}
import javafx.scene.layout.{StackPane, VBox}
import tigerjython.execute.PythonInstallations

/**
 * This contains preferences related to Python, i.e. the execution of the Python scripts.
 *
 * @author Tobias Kohn
 */
class PythonPreferencesPane extends PreferencePane {

  val caption: StringProperty = new SimpleStringProperty("Python")

  protected def createInstallationChooser(): Seq[Node] = {
    val label = new Label("Choose an installation:")
    val chooser = new ComboBox[String]()
    chooser.getItems.addAll(PythonInstallations.getAvailableVersions: _*)
    chooser.getSelectionModel.select(PythonInstallations.getSelectedIndex)
    chooser.valueProperty.addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
        PythonInstallations.select(newValue)
      }
    })
    label.labelForProperty.setValue(chooser)
    Seq(label, chooser)
  }

  override lazy val node: Node = {
    val result = new VBox()
    result.getChildren.addAll(createInstallationChooser(): _*)
    new StackPane(result)
  }
}
