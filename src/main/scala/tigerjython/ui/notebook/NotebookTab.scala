/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.notebook

import java.io.File

import javafx.application.Platform
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.geometry.{Orientation, Pos}
import javafx.scene.{Node, shape}
import javafx.scene.control.{Button, MenuButton, MenuItem, ScrollPane, SeparatorMenuItem, SplitPane, TextArea, ToolBar}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{BorderPane, HBox, Priority, StackPane, VBox}
import javafx.scene.paint.Color
import javafx.scene.shape.{Polygon, Rectangle}
import tigerjython.core.Preferences
import tigerjython.execute.{Evaluator, ExecutionController, Executor, ExecutorFactory, InterpreterInstallations, TigerJythonExecutorFactory}
import tigerjython.ui.{ImagePool, TabFrame, ZoomMixin}
import tigerjython.ui.Utils.onFX

/**
 * @author Tobias Kohn
 */
class NotebookTab extends TabFrame with ExecutionController {

  protected val itemsBox: VBox with ZoomMixin = new VBox with ZoomMixin
  protected val outputPane: TextArea = createOutputPane
  protected val scrollPane: ScrollPane = new ScrollPane()
  protected val splitPane: SplitPane = new SplitPane()
  protected val topToolBar: Node = createTopToolBar

  protected lazy val targetImage: ImageView = {
    val result = new ImageView(ImagePool.tigerJython_Logo)
    result.setFitHeight(16)
    result.setFitWidth(16)
    result
  }
  protected lazy val targetButton: MenuButton = new MenuButton()

  protected var evalFactory: ExecutorFactory = TigerJythonExecutorFactory
  protected var evaluator: Evaluator = _

  protected val cells: collection.mutable.ArrayBuffer[NotebookCell] = collection.mutable.ArrayBuffer[NotebookCell]()

  protected var cellIndex: Int = -1

  caption.setValue("Notebook")

  {
    getStyleClass.add("notebook")
    itemsBox.getStyleClass.add("cells")
    val mainBox = new BorderPane()
    val itemsPane = new StackPane()
    itemsPane.getChildren.add(itemsBox)
    StackPane.setAlignment(itemsBox, Pos.TOP_CENTER)
    scrollPane.setContent(itemsPane)
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER)
    splitPane.setOrientation(Orientation.VERTICAL)
    splitPane.getItems.add(scrollPane)
    splitPane.getItems.add(outputPane)
    mainBox.setTop(topToolBar)
    mainBox.setCenter(splitPane)
    mainBox.prefHeightProperty.bind(this.heightProperty)
    splitPane.prefWidthProperty.bind(this.widthProperty)
    getChildren.add(mainBox)
    itemsBox.prefWidthProperty().bind(widthProperty())
    addCell()
  }
  addEventFilter(MouseEvent.MOUSE_CLICKED, (event: MouseEvent) => {
    event.getTarget match {
      case cell: NotebookCell =>
        selectCell(cell)
      case _ =>
    }
  })
  itemsBox.zoomProperty.addListener(new ChangeListener[Number] {
    override def changed(observableValue: ObservableValue[_ <: Number], t: Number, newZoom: Number): Unit =
      Platform.runLater(() => {
        val size: Double = Preferences.fontSize.get * newZoom.doubleValue()
        for (cell <- cells)
          cell.setTextSize(size)
      })
  })

  def addCell(): NotebookCell =
    addCell(NotebookCell(this))

  def addCell(cell: NotebookCell): NotebookCell =
    if (cell != null) {
      cells += cell
      if (cells.length > 1) {
        val r = new shape.Rectangle(10, 10)
        r.setFill(Color.TRANSPARENT)
        r.setStroke(Color.TRANSPARENT)
        itemsBox.getChildren.add(r)
      }
      val size = Preferences.fontSize.get * itemsBox.zoomProperty.getValue
      cell.setTextSize(size)
      itemsBox.getChildren.add(cell)
      selectCell(cell)
      cell
    } else
      null

  def appendToErrorOutput(s: String): Unit = {}

  protected def createOutputPane: TextArea = {
    val result = new TextArea()
    result.getStyleClass.add("output-pane")
    result
  }

  protected def createTargetMenuItem(name: String, img: Image, factory: ExecutorFactory): MenuItem = {
    val result = new MenuItem(name)
    result.setOnAction(_ => {
      targetImage.setImage(img)
      evalFactory = factory
    })
    result.setUserData(factory)
    result.setGraphic(new ImageView(img))
    result
  }

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
    runButton.setOnAction(_ => { evaluateCell() })
    //stopButton.setOnAction(_ => { stop() })
    val filler = new HBox()
    HBox.setHgrow(filler, Priority.ALWAYS)
    targetButton.setGraphic(targetImage)
    for (interpreter <- InterpreterInstallations.availableInterpreters) {
      if (interpreter.title== "-")
        targetButton.getItems.add(new SeparatorMenuItem())
      else if (interpreter.factory != null && interpreter.factory.canEvaluate)
        targetButton.getItems.add(createTargetMenuItem(interpreter.title, interpreter.icon, interpreter.factory))
    }
    result.getItems.addAll(runButton, filler, targetButton)
    result
  }

  def currentCell: NotebookCell =
    if (0 <= cellIndex && cellIndex < cells.length)
      cells(cellIndex)
    else if (cells.nonEmpty) {
      selectCell(-1)
      currentCell
    } else
      null

  def displayError(line: Int, msg: String): Unit = {
//    displayError(line, 0, msg)
  }

  override def focusChanged(receivingFocus: Boolean): Unit = {
    if (receivingFocus)
      onFX(() => {
        // At the time this is executed, requesting the focus will likely fail.  We therefore need this dirty trick to
        // try again a little bit later on.
        Platform.runLater(() => {
          val cell = currentCell
          if (cell != null)
            cell.requestFocus()
        })
      })
  }

  def evaluateCell(): Unit = {
    val cell = currentCell
    if (evaluator == null)
      evalFactory.createEvaluator(this, evaluator=>{
        this.evaluator = evaluator
        if (cell != null)
          cell.evaluate(evaluator)
      })
    else if (cell != null)
      cell.evaluate(evaluator)
  }

  override def onClose(): Unit = {
    if (evaluator != null)
      evaluator.shutdown()
  }

  def selectCell(index: Int): Unit =
    if (0 <= index && index < cells.length && cellIndex != index) {
      if (0 <= cellIndex && cellIndex < cells.length)
        cells(cellIndex).setActive(false)
      cellIndex = index
      onFX(() => {
        currentCell.setActive(true)
      })
    } else
    if (index == -1 && cells.nonEmpty)
      selectCell(cells.length - 1)

  def selectCell(cell: NotebookCell): Unit =
    if (cell != currentCell)
      selectCell(cells.indexOf(cell))

  def selectNextCell(): Unit = {
    val idx = cellIndex + 1
    if (idx < cells.length)
      selectCell(idx min cells.length)
    else
      addCell()
  }

  def appendToLog(text: String): Unit = {
    // println(text)
  }

  def appendToOutput(text: String): Unit =
    outputPane.appendText(text)

  def clearOutput(): Unit = {}

  def getExecutableFile: File = null

  def getText: String = null

  def updateRunStatus(executor: Executor, running: Boolean): Unit = {}
}
object NotebookTab {

  def apply(): NotebookTab = new NotebookTab()
}