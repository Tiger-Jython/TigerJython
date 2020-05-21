/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.editor

import java.io.{FileWriter, PrintWriter}

import javafx.application.Platform
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.geometry.Orientation
import javafx.scene._
import javafx.scene.control.{Alert, Button, ButtonType, Label, SplitPane, TabPane, ToolBar}
import javafx.scene.layout.BorderPane
import javafx.scene.shape.{Polygon, Rectangle}
import javafx.stage.Popup
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.CodeArea
import tigerjython.errorhandling._
import tigerjython.execute.PythonExecutor
import tigerjython.ui.{TabFrame, TigerJythonApplication, ZoomMixin}

/**
 * The editor is the central frame to be displayed in the main window.  It provides an editor, output-panes and a
 * toolbar to run or stop a program.
 *
 * @author Tobias Kohn
 */
abstract class EditorTab extends TabFrame {

  import tigerjython.ui.Utils._

  protected val editor: CodeArea = createEditorNode
  protected val errorPane: OutputPane = createErrorPane
  protected var errorPopup: Popup = _
  protected val infoPane: TabPane = new TabPane()
  protected val outputPane: OutputPane = createOutputPane
  protected val scrollPane: VirtualizedScrollPane[_] = new VirtualizedScrollPane(editor)
  protected val sideMenuBar: Node = createSideMenuBar
  protected val splitPane: SplitPane = new SplitPane()
  protected val topToolBar: Node = createTopToolBar

  protected var file: java.io.File = _
  protected var executor: PythonExecutor = _
  private var _execFile: java.io.File = _
  private var _running: Boolean = false

  protected var errorLabel: Node = _

  private object CaretChangeListener extends ChangeListener[Integer] {

    override def changed(observableValue: ObservableValue[_ <: Integer], t: Integer, t1: Integer): Unit =
      hideError()
  }

  private object TextChangeListener extends ChangeListener[String] {

    override def changed(observableValue: ObservableValue[_ <: String], t: String, t1: String): Unit =
      hideError()
  }

  { // Create the scene/graphical contents
    val mainBox = new BorderPane()
    mainBox.setTop(topToolBar)
    infoPane.getTabs.addAll(outputPane, errorPane)
    splitPane.setOrientation(Orientation.VERTICAL)
    splitPane.getItems.addAll(scrollPane, infoPane)
    mainBox.setCenter(splitPane)
    mainBox.prefHeightProperty.bind(this.heightProperty)
    splitPane.prefWidthProperty.bind(this.widthProperty)
    editor.prefWidthProperty.bind(mainBox.widthProperty)
    editor.prefHeightProperty.bind(mainBox.heightProperty.subtract(infoPane.heightProperty))
    getChildren.add(mainBox)
  }

  def appendToErrorOutput(s: String): Unit =
    errorPane.append(s)

  def appendToOutput(s: String): Unit =
    outputPane.append(s)

  def autoSave(): Unit = save()

  def clearOutput(): Unit = {
    outputPane.clear()
    errorPane.clear()
  }

  protected def createEditorNode: CodeArea

  protected def createErrorLabel(line: Int, msg: String): Node = {
    val result = new Label(msg)
    result.getStyleClass.add("error-label")
    editor match {
      case zoomMixin: ZoomMixin =>
        result.setStyle("-fx-font-size: %g%%;".format(zoomMixin.getZoom * 100))
      case _ =>
    }
    result
  }

  protected def createErrorPane: OutputPane =
    new DefaultOutputPane(this, "problems")

  protected def createOutputPane: OutputPane = {
    val result = new DefaultOutputPane(this, "output")
    result.onKeyPress = {
      key =>
        if (executor != null)
          executor.writeToInput(key)
    }
    result
  }

  protected def createSideMenuBar: Node = null

  protected def createTopToolBar: Node = {
    val result = new ToolBar()
    val runButton = new Button()
    val runTriangle = new Polygon()
    runTriangle.getPoints.addAll( -5.0, 8.0, -5.0, -8.0, 6.0, 0.0 )
    runTriangle.getStyleClass.add("run-triangle")
    runButton.setGraphic(runTriangle)
    val stopButton = new Button()
    val stopRect = new Rectangle(13, 13)
    stopRect.getStyleClass.add("stop-square")
    stopButton.setGraphic(stopRect)
    runButton.setOnAction(_ => { run() })
    stopButton.setOnAction(_ => { stop() })
    result.getItems.addAll(runButton, stopButton)
    result
  }

  def displayError(line: Int, msg: String): Unit =
    displayError(line, 0, msg)

  def displayError(line: Int, column: Int, msg: String): Unit = {
    onFX(() => {
      editor.requestFocus()
      editor.moveTo(line, column)
      val caretBounds = editor.caretBoundsProperty().getValue
      if (caretBounds.isPresent) {
        val popup = new Popup()
        popup.getContent.add(createErrorLabel(line, msg))
        popup.setX(caretBounds.get.getMinX)
        popup.setY(caretBounds.get.getMaxY)
        popup.show(TigerJythonApplication.mainStage)
        errorPopup = popup
        editor.caretPositionProperty().addListener(CaretChangeListener)
        editor.textProperty().addListener(TextChangeListener)
      }
    })
  }

  override def focusChanged(receivingFocus: Boolean): Unit = {
    if (receivingFocus)
      onFX(() => {
        // At the time this is executed, requesting the focus will likely fail.  We therefore need this dirty trick to
        // try again a little bit later on.
        Platform.runLater(() => {
          editor.requestFocus()
        })
      })
    else
      hideError()
  }

  def getCaretPosition: Int =
    editor.getCaretPosition

  def getExecutableFile: java.io.File =
    _execFile

  def getFile: java.io.File = file

  def getSelectedText: String =
    editor.getSelectedText

  def getText: String =
    editor.getText

  def hasExecutableFile: Boolean =
    if (file != null) {
      save()
      _execFile = file
      true
    } else
    if (!isEmpty) {
      if (_execFile == null) {
        _execFile = java.io.File.createTempFile(caption.getValue, ".py")
        _execFile.deleteOnExit()
      }
      val writer = new FileWriter(_execFile)
      val printer = new PrintWriter(writer)
      printer.print(editor.getText())
      printer.close()
      true
    } else
      false

  def handleError(errorText: String): Unit = {
    infoPane.getSelectionModel.select(1)
    val (line, filename, msg) = PythonRuntimeErrors.generateMessage(errorText)
    if (line >= 0 && msg != null)
      displayError(line-1, msg)
  }

  def hideError(): Unit =
    if (errorPopup != null) {
      errorPopup.hide()
      errorPopup = null
      editor.caretPositionProperty().removeListener(CaretChangeListener)
      editor.textProperty().removeListener(TextChangeListener)
    }

  def isEmpty: Boolean =
    file == null && !editor.isUndoAvailable && editor.getText() == ""

  def isReadonly: Boolean = false

  def isRunning: Boolean = _running

  def loadFile(file: java.io.File): Unit = {
    if (file.canWrite)
      this.file = file
    val source = scala.io.Source.fromFile(file)
    editor.replaceText(source.getLines.mkString("\n"))
    caption.setValue(file.getName)
  }

  def run(): Unit = {
    if (hasExecutableFile) {
      outputPane.clear()
      errorPane.clear()
      infoPane.getSelectionModel.select(0)

      // Check syntax
      onFX(() => {
        StaticErrorChecker.checkSyntax(caption.get(), editor.getText()) match {
          case Some((line, offs, msg)) =>
            displayError(line, offs, msg)
          case None =>
            Platform.runLater(() => _run())
        }
      })
    } else
      new Alert(Alert.AlertType.ERROR, "Please save the file first!", ButtonType.OK).showAndWait()
  }

  protected def _run(): Unit = {
    // Execute the code
    val executor = PythonExecutor(this)
    if (executor != null)
      executor.run()
    else
      new Alert(Alert.AlertType.ERROR, "Please choose an appropriate interpreter", ButtonType.OK).showAndWait()
  }

  def save(): Unit =
    if (file != null) synchronized {
      val writer = new FileWriter(file)
      val printer = new PrintWriter(writer)
      printer.print(editor.getText())
      printer.close()
    }

  def setFile(file: java.io.File): Unit = {
    this.file = file
    if (file != null) {
      caption.setValue(file.getName)
      save()
    }
  }

  def setSelectedText(s: String): Unit =
    editor.replaceSelection(s)

  def stop(): Unit = {
    if (executor != null)
      executor.stop()
  }

  def updateRunStatus(executor: PythonExecutor, running: Boolean): Unit =
    if (running) {
      this.executor = executor
      _running = true
    } else {
      _running = false
      this.executor = null
      onFX(() => {
        val errorText = errorPane.getContentText
        if (errorText != "")
          handleError(errorText)
      })
    }
}
