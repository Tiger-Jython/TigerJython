/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui

import javafx.application.{Application, Platform, Preloader}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene._
import javafx.scene.control._
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.stage.{Stage, StageStyle}
import tigerjython.core.{BuildInfo, Configuration, Preferences}
import tigerjython.files.Documents
import tigerjython.plugins.{MainWindow, PluginsManager}

import com.sun.javafx.application.LauncherImpl

/**
 * This is the _JavaFX_ application that creates the entire UI for the IDE.
 *
 * @author Tobias Kohn
 */
object TigerJythonApplication {

  private var _application: TigerJythonApplication = _

  private var _mainStage: Stage = _

  private var _mainWindow: MainWindow = _

  private var _scene: Scene = _

  private var _tabManager: TabManager = _

  private var _zoomingPane: ZoomingPane = _

  def currentApplication: TigerJythonApplication = _application

  def mainScene: Scene = _scene

  def mainStage: Stage = _mainStage

  def mainWindow: MainWindow = _mainWindow

  def tabManager: TabManager = _tabManager

  /**
   * Launches the IDE.
   *
   * @param args  Arguments to be passed to the user interface (usually empty).
   */
  def launchApplication(args: Array[String]): Unit = {
    // Application.launch(classOf[TigerJythonApplication], args: _*)
    LauncherImpl.launchApplication(classOf[TigerJythonApplication], classOf[TigerJythonPreloader], args)
  }
}

class TigerJythonApplication extends Application {

  import TigerJythonApplication.{_application, _mainStage, _mainWindow, _scene, _tabManager, _zoomingPane}

  _application = this

  //private var splash: Stage = _

  lazy val menuManager: MenuManager = new DefaultMenuManager(this)

  lazy val tabManager: TabPane with TabManager = new DefaultTabManager()

  override def start(primaryStage: Stage): Unit = {
    notifyPreloader(new Preloader.ProgressNotification(0.125))

    initialize(() => {
      val root = new BorderPane()
      // Connect the main window's size with persistent properties
      root.setPrefWidth(Preferences.windowWidth.get)
      root.setPrefHeight(Preferences.windowHeight.get)
      Preferences.windowWidth.bind(root.widthProperty())
      Preferences.windowHeight.bind(root.heightProperty())

      //root.setTop(menuManager.mainMenu)
      val contents = new BorderPane()
      //contents.setTop(menuManager.mainMenu)
      contents.setCenter(tabManager)
      val centre = new ZoomingPane()
      centre.getChildren.add(contents)
      root.setCenter(centre)
      _zoomingPane = centre
      centre.zoomFactorProperty.bind(Preferences.globalZoom)

      val scene = new Scene(root)
      scene.getStylesheets.add("themes/%s.css".format(Preferences.theme.get()))
      scene.focusOwnerProperty().addListener(new ChangeListener[Node] {
        override def changed(observableValue: ObservableValue[_ <: Node], oldNode: Node, newNode: Node): Unit = {
          menuManager.focusChanged(newNode)
          tabManager.focusChanged(newNode)
        }
      })
      Preferences.theme.addListener(new ChangeListener[String] {
        override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
          Platform.runLater(() => {
            scene.getStylesheets.add("themes/%s.css".format(newValue))
            scene.getStylesheets.remove("themes/%s.css".format(oldValue))
          })
        }
      })
      val mainStage = primaryStage
      mainStage.setScene(scene)
      mainStage.setTitle(BuildInfo.Name + " " + BuildInfo.fullVersion)
      mainStage.setOnCloseRequest(_ => handleCloseRequest())
      mainStage.getIcons.addAll(
        new Image(getClass.getClassLoader.getResourceAsStream("resources/%s_32.png".format(Configuration.appLogoName))),
        new Image(getClass.getClassLoader.getResourceAsStream("resources/%s_64.png".format(Configuration.appLogoName))),
        new Image(getClass.getClassLoader.getResourceAsStream("resources/%s_128.png".format(Configuration.appLogoName))),
      )
      mainStage.show()
      _mainStage = mainStage
      _scene = scene
      _mainWindow = new MainWindow(menuManager, tabManager)
      _tabManager = tabManager
      Documents.initialize()
      notifyPreloader(new Preloader.ProgressNotification(1.0))
      Platform.runLater(() => {
        PluginsManager.initialize()
      })
    })
  }

  def getFocusedControl: Node =
    if (_scene != null)
      _scene.getFocusOwner
    else
      null

  protected def initialize(cc: Runnable): Unit = {
    new Thread(() => {
      tigerjython.execute.PythonInstallations.initialize()
      tigerjython.core.Configuration.initialize()
      tigerjython.remote.ExecuteServer.initialize()
      tigerjython.execute.TigerJythonProcess.initialize()

      UIString.loadFromResource(Preferences.languageCode.get)
      Preferences.languageCode.addListener(new ChangeListener[String] {
        override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit =
          if (oldValue != newValue) {
            UIString.loadFromResource(newValue)
          }
      })
      Platform.runLater(cc)
    }).start()
  }

  def handleCloseRequest(): Unit = {
    tabManager.saveAll()
    stop()
    tigerjython.execute.TigerJythonProcess.shutdown()
    tigerjython.execute.TigerJythonExecutorFactory.shutdown()
    tigerjython.remote.ExecuteServer.quit()
    editing.SyntaxHighlighter.shutdown()
    editing.BackgroundSaver.shutdown()
    Platform.exit()
    Thread.sleep(500)
    sys.exit()
  }

  def showPreferences(): Unit = {
    tabManager.showOrAdd(preferences.PreferencesTab())
  }

 /* override def stop(): Unit = {
    executor.shutdown()
  }*/
}