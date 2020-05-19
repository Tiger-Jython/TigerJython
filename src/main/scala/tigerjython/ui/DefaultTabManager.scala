/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.Node
import javafx.scene.control.{Tab, TabPane}

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
          case frame: TabFrame =>
            frame.focusChanged(true)
        }
    }
  })

  def addTab(frame: TabFrame): Option[TabFrame] =
    if (frame != null) {
      val tab = createTab(frame)
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

  def showOrAdd(frame: TabFrame): Unit = {
    for (t <- getTabs.asScala)
      t match {
        case tab: Tab if tab.getContent eq frame =>
          getSelectionModel.select(tab)
          tab.getContent.setVisible(true)
        case _ =>
      }
    addTab(frame)
  }

  /*def tabChanged(sender: TabFrame): Unit = {
  } if (sender != null && sender.manager == this) {
      for (t <- getTabs.asScala)
        t match {
          case tab: Tab if tab.getContent == sender =>
            if (Platform.isFxApplicationThread)
              tab.setText(sender.caption)
            else
              Platform.runLater(() => {
                tab.setText(sender.caption)
              })
          case _ =>
        }
    }
  }*/
}
