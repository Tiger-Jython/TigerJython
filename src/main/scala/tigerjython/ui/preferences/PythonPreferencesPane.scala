/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.preferences

import javafx.beans.property.{SimpleIntegerProperty, SimpleStringProperty, StringProperty}
import javafx.beans.value.ObservableValue
import javafx.scene.control.TableColumn.CellDataFeatures
import javafx.scene.Node
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, Button, CheckBox, Label, TableColumn, TableView, Tooltip}
import javafx.scene.layout.{HBox, Priority, Region, StackPane, VBox}
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import javafx.util.Callback
import tigerjython.core.Preferences
import tigerjython.execute.{InterpreterInstallations, PythonInstallations}
import tigerjython.ui.{TigerJythonApplication, UIString}
import tigerjython.utils.OSPlatform

/**
 * This contains preferences related to Python, i.e. the execution of the Python scripts.
 *
 * @author Tobias Kohn
 */
class PythonPreferencesPane extends PreferencePane {

  protected case class PythonInterpreterInfo(name: String, version: Int, path: String, isCustom: Boolean)

  val caption: StringProperty = new SimpleStringProperty("Python")

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

  protected lazy val interpreterTable: TableView[PythonInterpreterInfo] = {
    val table = new TableView[PythonInterpreterInfo]()
    val nameColumn = new TableColumn[PythonInterpreterInfo, String]("Name")
    val pathColumn = new TableColumn[PythonInterpreterInfo, String]("Path")
    val versionColumn = new TableColumn[PythonInterpreterInfo, Number]("Version")
    nameColumn.setCellValueFactory(new Callback[CellDataFeatures[PythonInterpreterInfo, String], ObservableValue[String]]() {
      def call(p: CellDataFeatures[PythonInterpreterInfo, String]): ObservableValue[String] = {
        new SimpleStringProperty(p.getValue.name)
      }
    })
    pathColumn.setCellValueFactory(new Callback[CellDataFeatures[PythonInterpreterInfo, String], ObservableValue[String]]() {
      def call(p: CellDataFeatures[PythonInterpreterInfo, String]): ObservableValue[String] = {
        new SimpleStringProperty(p.getValue.path)
      }
    })
    versionColumn.setCellValueFactory(new Callback[CellDataFeatures[PythonInterpreterInfo, Number], ObservableValue[Number]]() {
      def call(p: CellDataFeatures[PythonInterpreterInfo, Number]): ObservableValue[Number] = {
        new SimpleIntegerProperty(p.getValue.version)
      }
    })
    versionColumn.setStyle( "-fx-alignment: center-right;")
    table.getColumns.addAll(nameColumn, versionColumn, pathColumn)
    table
  }

  private def addPythonVersion(): Unit = {
    val selectedFile = fileChooser.showOpenDialog(TigerJythonApplication.mainStage)
    if (selectedFile != null)
      PythonInstallations.add(selectedFile, () => updateInterpreterSelection(true))
  }

  private def removePythonVersion(): Unit = {
    val index = interpreterTable.getSelectionModel.getFocusedIndex
    if (index >= 0) {
      val info = interpreterTable.getItems.get(index)
      if (info.isCustom) {
        PythonInstallations.removeCustomInterpreter(info.name)
        updateInterpreterSelection(true)
      } else
        new Alert(AlertType.ERROR, "Cannot remove '%s'.".format(info.name)).showAndWait()
    }
  }

  protected def updateInterpreterSelection(changed: Boolean): Unit = {
    interpreterTable.getItems.clear()
    for ((name, version, path) <- PythonInstallations.getAvailableSystems) {
      interpreterTable.getItems.add(PythonInterpreterInfo(name, version, path.toAbsolutePath.toString,
        PythonInstallations.isCustomInterpreter(name)))
    }
    if (changed) {
      InterpreterInstallations.invalidateCache()
      TigerJythonApplication.tabManager.reloadExecutionTargets()
    }
  }

  protected def createInstallationChooser(): Seq[Node] = {
    val label = new Label("Python Interpreters:")
    val addButton = new Button("+")
    val removeButton = new Button("–")
    val hspace = new Region()
    HBox.setHgrow(hspace, Priority.ALWAYS)
    val toolBar = new HBox(label, hspace, addButton, removeButton)
    updateInterpreterSelection(false)
    label.labelForProperty.setValue(interpreterTable)
    addButton.setOnAction(_ => { addPythonVersion() })
    removeButton.setOnAction(_ => { removePythonVersion() })
    Seq(new Label("  "), toolBar, interpreterTable)
  }

  protected def createErrorCheckOptions(): Seq[Node] = {
    val checkErrors = new CheckBox("Check for syntax errors")
    val strictErrorChecking = new CheckBox("Strict checking")
    UIString("prefs.jython.exterrors") += checkErrors.textProperty()
    UIString("prefs.jython.strictchecking") += strictErrorChecking.textProperty()
    checkErrors.setSelected(Preferences.checkSyntax.get)
    checkErrors.selectedProperty().bindBidirectional(Preferences.checkSyntax)
    strictErrorChecking.setSelected(Preferences.syntaxCheckIsStrict.get)
    strictErrorChecking.selectedProperty().bindBidirectional(Preferences.syntaxCheckIsStrict)
    strictErrorChecking.disableProperty().bind(checkErrors.selectedProperty().not())
    Seq(checkErrors, strictErrorChecking)
  }

  protected def createExtOptions(): Seq[Node] = {
    val repeatLoop = new CheckBox("Repeat loop")
    val waitWindowClosed = new CheckBox("Wait for windows to be closed")
    UIString("prefs.jython.repeatloop") += repeatLoop.textProperty()
    repeatLoop.setSelected(Preferences.repeatLoop.get)
    repeatLoop.selectedProperty().bindBidirectional(Preferences.repeatLoop)
    waitWindowClosed.setSelected(Preferences.waitForWindowClosed.get)
    waitWindowClosed.selectedProperty().bindBidirectional(Preferences.waitForWindowClosed)
    Seq(repeatLoop, waitWindowClosed)
  }

  override lazy val node: Node = {
    val result = new VBox()
    result.getChildren.addAll(createErrorCheckOptions(): _*)
    result.getChildren.addAll(createExtOptions(): _*)
    result.getChildren.addAll(createInstallationChooser(): _*)
    result.setSpacing(4)
    new StackPane(result)
  }
}
