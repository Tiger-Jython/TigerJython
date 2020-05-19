/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui

import javafx.application.{Application, Platform}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene._
import javafx.scene.control._
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import tigerjython.core.{BuildInfo, Preferences}

/**
 * This is the _JavaFX_ application that creates the entire UI for the IDE.
 *
 * @author Tobias Kohn
 */
object TigerJythonApplication {

  private var _mainStage: Stage = _

  private var _scene: Scene = _

  private var _zoomingPane: ZoomingPane = _

  def mainScene: Scene = _scene

  def mainStage: Stage = _mainStage

  /**
   * Launches the IDE.
   *
   * @param args  Arguments to be passed to the user interface (usually empty).
   */
  def launchApplication(args: Array[String]): Unit = {
    Application.launch(classOf[TigerJythonApplication], args: _*)
  }
}

class TigerJythonApplication extends Application {

  import TigerJythonApplication.{_mainStage, _scene, _zoomingPane}

  lazy val menuManager: MenuManager = new DefaultMenuManager(this)

  lazy val tabManager: TabPane with TabManager = new DefaultTabManager()

  override def start(primaryStage: Stage): Unit = {
    initialize()

    val root = new BorderPane()
    // Connect the main window's size with persistent properties
    root.setPrefWidth(Preferences.windowWidth.get)
    root.setPrefHeight(Preferences.windowHeight.get)
    Preferences.windowWidth.bind(root.widthProperty())
    Preferences.windowHeight.bind(root.heightProperty())

    //root.setTop(menuManager.mainMenu)
    val contents = new BorderPane()
    contents.setTop(menuManager.mainMenu)
    contents.setCenter(tabManager)
    val centre = new ZoomingPane()
    centre.getChildren.add(contents)
    root.setCenter(centre)
    _zoomingPane = centre
    centre.zoomFactorProperty.bind(Preferences.globalZoom)
    tabManager.addTab(editor.PythonEditorTab())

    val scene = new Scene(root)
    scene.getStylesheets.add("themes/python-keywords.css")
    scene.focusOwnerProperty().addListener(new ChangeListener[Node] {
      override def changed(observableValue: ObservableValue[_ <: Node], oldNode: Node, newNode: Node): Unit = {
        menuManager.focusChanged(newNode)
        tabManager.focusChanged(newNode)
      }
    })
    primaryStage.setScene(scene)
    primaryStage.setTitle(BuildInfo.Name)
    primaryStage.setOnCloseRequest(_ => handleCloseRequest())
    primaryStage.show()
    _mainStage = primaryStage
    _scene = scene
  }

  def getFocusedControl: Node =
    if (_scene != null)
      _scene.getFocusOwner
    else
      null

  protected def initialize(): Unit = {
    UIString.loadFromResource(Preferences.languageCode.get)
    Preferences.languageCode.addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit =
        if (oldValue != newValue) {
          UIString.loadFromResource(newValue)
        }
    })
  }

  def handleCloseRequest(): Unit = {
    stop()
    Platform.exit()
    sys.exit()
  }

  def showPreferences(): Unit = {
    tabManager.showOrAdd(preferences.PreferencesTab())
  }

 /* override def stop(): Unit = {
    executor.shutdown()
  }*/
}