/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui

import javafx.application.Platform
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.Node
import javafx.scene.control.{Tab, TabPane}
import tigerjython.core.Preferences
import tigerjython.files.Document
import tigerjython.ui.editor.{EditorTab, PythonEditorTab}
import tigerjython.ui.tabs.OpenDocumentTab

import scala.jdk.CollectionConverters._

/**
 * The default/standard implementation of the `TabManager`, used to manage the different tabs displayed in the main
 * window.
 *
 * @author Tobias Kohn
 */
class DefaultTabManager extends TabPane with TabManager {

  getSelectionModel.selectedItemProperty.addListener(new ChangeListener[Tab] {
    override def changed(observableValue: ObservableValue[_ <: Tab], oldTab: Tab, newTab: Tab): Unit = {
      if (oldTab != null)
        oldTab.getContent match {
          case frame: TabFrame =>
            frame.focusChanged(false)
          case _ =>
        }
      if (newTab != null)
        newTab.getContent match {
          case frame: EditorTab =>
            frame.focusChanged(true)
            Preferences.selectedDocument.setValue(frame.getDocumentName)
          case frame: TabFrame =>
            frame.focusChanged(true)
          case _  =>
        }
    }
  })

  {
    val frame = OpenDocumentTab()
    val t = new Tab()
    t.setContent(frame)
    t.setClosable(false)
    t.textProperty().bind(frame.caption)
    getTabs.add(t)
  }

  def addTab(frame: TabFrame): Option[TabFrame] =
    if (frame != null) {
      val tab = createTab(frame)
      val tabList = getTabs
      val len = tabList.size()
      if (len > 0)
        tabList.add(len-1, tab)
      else
        getTabs.add(tab)
      if (frame.manager != null) {}
      frame.manager = this
      getSelectionModel.select(tab)
      Some(frame)
    } else
      None

  private def createTab(frame: TabFrame): Tab = {
    val result = new Tab()
    result.setContent(frame)
    result.textProperty.bind(frame.caption)
    result.setOnCloseRequest({ _ => frame.onClose() })
    result
  }

  def closeTab(frame: TabFrame): Unit =
    if (frame != null && frame.manager == this) {
      for (t <- getTabs.asScala)
        t match {
          case tab: Tab if tab.getContent == frame =>
            getTabs.remove(tab)
            frame.manager = null
          case _ =>
        }
    }

  def currentFrame: Option[TabFrame] = {
    val item = getSelectionModel.getSelectedItem
    if (item != null)
      item.getContent match {
        case f: TabFrame =>
          return Some(f)
        case _ =>
      }
    None
  }

  def findTab(p: TabFrame=>Boolean): Option[TabFrame] = {
    for (t <- getTabs.asScala)
      t match {
        case tab: Tab =>
          tab.getContent match {
            case f: TabFrame if p(f) =>
              return Some(f)
            case _ =>
          }
        case _ =>
      }
    None
  }

  def focusChanged(receivingNode: Node): Unit =
    if (receivingNode eq this)
      currentFrame match {
        case Some(frame) =>
          frame.focusChanged(true)
        case None =>
      }

  def openDocument(document: Document): Unit = {
    if (document.frame == null) {
      val f = PythonEditorTab(document)
      showOrAdd(f)
    } else
      showOrAdd(document.frame)
  }

  def saveAll(): Boolean = {
    for (t <- getTabs.asScala)
      t match {
        case tab: Tab =>
          tab.getContent match {
            case editorTab: EditorTab =>
              editorTab.autoSave()
            case _ =>
          }
        case _ =>
      }
    true
  }

  def selectDocument(name: String = null): Unit =
    if (name == null) {
      val selected = Preferences.selectedDocument.get()
      if (selected != null && selected != "")
        selectDocument(selected)
    }
    else if (name != "") {
      for (t <- getTabs.asScala)
        t match {
          case tab: Tab =>
            tab.getContent match {
              case editorTab: EditorTab if editorTab.getDocumentName == name =>
                Platform.runLater(() => {
                  getSelectionModel.select(tab)
                })
                return
              case _ =>
            }
          case _=>
        }
    }

  def showOrAdd(frame: TabFrame): Unit = {
    for (t <- getTabs.asScala)
      t match {
        case tab: Tab if tab.getContent eq frame =>
          getSelectionModel.select(tab)
          tab.getContent.setVisible(true)
          return
        case _ =>
      }
    addTab(frame)
  }
}
