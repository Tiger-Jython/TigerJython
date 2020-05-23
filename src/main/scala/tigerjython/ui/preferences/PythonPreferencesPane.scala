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
import javafx.scene.control.{Button, ComboBox, Label}
import javafx.scene.layout.{StackPane, VBox}
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import tigerjython.execute.PythonInstallations
import tigerjython.ui.TigerJythonApplication
import tigerjython.utils.OSPlatform

/**
 * This contains preferences related to Python, i.e. the execution of the Python scripts.
 *
 * @author Tobias Kohn
 */
class PythonPreferencesPane extends PreferencePane {

  val caption: StringProperty = new SimpleStringProperty("Python")

  protected lazy val interpreterSelectionBox: ComboBox[String] = new ComboBox[String]()

  protected lazy val fileChooser: FileChooser = {
    val result = new FileChooser()
    if (OSPlatform.system == OSPlatform.WINDOWS)
      result.getExtensionFilters.add(
        new ExtensionFilter("Executables", "*.exe")
      )
    result.getExtensionFilters.add(
      new ExtensionFilter("All Files", "*.*")
    )
    result
  }

  private def addPythonVersion(): Unit = {
    val selectedFile = fileChooser.showOpenDialog(TigerJythonApplication.mainStage)
    if (selectedFile != null)
      PythonInstallations.add(selectedFile, () => updateInterpreterSelection())
  }

  protected def updateInterpreterSelection(): Unit = {
    interpreterSelectionBox.getItems.clear()
    interpreterSelectionBox.getItems.addAll(PythonInstallations.getAvailableVersions: _*)
    interpreterSelectionBox.getSelectionModel.select(PythonInstallations.getSelectedIndex)
  }

  protected def createInstallationChooser(): Seq[Node] = {
    val label = new Label("Choose an installation:")
    val addButton = new Button("+")
    updateInterpreterSelection()
    interpreterSelectionBox.valueProperty.addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
        PythonInstallations.select(newValue)
      }
    })
    label.labelForProperty.setValue(interpreterSelectionBox)
    addButton.setOnAction({ _ => addPythonVersion() })
    Seq(label, interpreterSelectionBox, addButton)
  }

  override lazy val node: Node = {
    val result = new VBox()
    result.getChildren.addAll(createInstallationChooser(): _*)
    new StackPane(result)
  }
}
