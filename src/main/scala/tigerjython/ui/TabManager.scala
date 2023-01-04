/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui

import javafx.scene.Node
import tigerjython.core.Preferences
import tigerjython.files.Document

/**
 * The `TabManager` is responsible for managing the different tabs in the main window.  It has a default implementation
 * with `DefaultTabManager`, but this interface allows to create and install a custom manager.
 *
 * @author Tobias Kohn
 */
trait TabManager {

  /**
   *
   * @param frame
   * @return
   */
  def addTab(frame: TabFrame): Option[TabFrame]

  def saveAll(): Boolean

  /**
   *
   * @param frame
   */
  def closeTab(frame: TabFrame): Unit

  /**
   *
   * @return
   */
  def currentFrame: Option[TabFrame]

  /**
   *
   * @param p
   * @return
   */
  def findTab(p: TabFrame=>Boolean): Option[TabFrame]

  /**
   *
   * @param receivingNode
   */
  def focusChanged(receivingNode: Node): Unit

  /**
   * This will be called when a new execution target has been added or removed.
   */
  def reloadExecutionTargets(): Unit

  /**
   *
   * @param document
   */
  def openDocument(document: Document): Unit

  /**
   *
   * @param name
   */
  def selectDocument(name: String = null): Unit

  /**
   *
   * @param frame
   */
  def showOrAdd(frame: TabFrame): Unit
}