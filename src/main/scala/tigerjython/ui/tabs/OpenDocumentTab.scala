/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.tabs

import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.geometry.Orientation
import javafx.scene.Parent
import javafx.scene.control.{SplitPane, TextField, ToolBar}
import javafx.scene.layout.{BorderPane, Region, VBox}
import tigerjython.files.Documents
import tigerjython.ui.TabFrame
import tigerjython.utils.SearchFilter

/**
 * @author Tobias Kohn
 */
class OpenDocumentTab protected () extends TabFrame {

  caption.setValue("+")

  protected val documentItems: collection.mutable.ArrayBuffer[DocumentItem] =
    collection.mutable.ArrayBuffer[DocumentItem]()

  val filterText: StringProperty = new SimpleStringProperty()
  protected val contents: BorderPane = new BorderPane()
  protected val items: VBox = createItems()
  protected val preview: SimplePythonEditor = createPreview()
  protected val searchBar: Region = createSearchBar()
  protected val splitPane: SplitPane = new SplitPane()

  {
    splitPane.setOrientation(Orientation.HORIZONTAL)
    splitPane.getItems.addAll(items, preview)
    contents.setTop(searchBar)
    contents.setCenter(splitPane)
    getChildren.add(contents)
    parentProperty().addListener(new ChangeListener[Parent] {
      override def changed(observableValue: ObservableValue[_ <: Parent], oldParent: Parent, newParent: Parent): Unit = {
        newParent match {
          case parent: Region =>
            contents.prefWidthProperty().bind(parent.widthProperty())
            contents.prefHeightProperty().bind(parent.heightProperty())
          case _ =>
        }
      }
    })
    filterText.addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
        setSearchFilter(newValue)
      }
    })
  }

  protected def createItems(): VBox =
    new VBox()

  protected def createPreview(): SimplePythonEditor = {
    new SimplePythonEditor()
  }

  protected def createSearchBar(): ToolBar = {
    val result = new ToolBar()
    val findTextField = new TextField()
    findTextField.prefWidthProperty().bind(result.widthProperty().divide(1.5))
    findTextField.textProperty().bindBidirectional(filterText)
    result.getItems.add(findTextField)
    result
  }

  override def focusChanged(receiveFocus: Boolean): Unit =
    if (receiveFocus)
      update()

  def setPreviewText(text: String): Unit = {
    preview.replaceText(text)
  }

  def setSearchFilter(text: String): Unit =
    if (documentItems.nonEmpty) {
      val filter = SearchFilter(text)
      for (doc <- documentItems)
        doc match {
          case item: OpenDocumentItem =>
            val score = item.document.getSearchScore(filter)
            item.setVisible(score > 0)
          case _ =>
        }
      items.getChildren.clear()
      for (doc <- documentItems)
        if (doc.isVisible)
          items.getChildren.add(doc)
    }

  def update(): Unit = {
    documentItems.clear()
    filterText.setValue("")
    items.getChildren.clear()
    val open = new NewDocumentItem(this)
    documentItems += open
    items.getChildren.add(open)
    for (doc <- Documents.getListOfDocuments) {
      val item = new OpenDocumentItem(this, doc)
      documentItems += item
      items.getChildren.add(item)
    }
  }
}
object OpenDocumentTab {

  private lazy val _openDocTab: OpenDocumentTab = new OpenDocumentTab()

  def apply(): OpenDocumentTab = _openDocTab
}
