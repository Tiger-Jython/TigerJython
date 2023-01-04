package tigerjython.ui

import javafx.animation.FadeTransition
import javafx.application.Preloader
import javafx.application.Preloader.StateChangeNotification
import javafx.scene.{Scene, layout}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{AnchorPane, HBox}
import javafx.scene.paint.Color
import javafx.stage.{Screen, Stage, StageStyle}
import javafx.util.Duration

/**
 * The Preloader is responsible for displaying the splash screen.
 */
class TigerJythonPreloader extends Preloader {

  private var imgView: ImageView = _
  private var splash: Stage = _

  override def start(stage: Stage): Unit = {
    splash = stage
    val img = new Image(getClass.getClassLoader.getResourceAsStream("resources/splash.png"))
    val bounds = Screen.getPrimary.getBounds
    imgView = new ImageView(img)
    val box = new AnchorPane(imgView)
    box.setStyle("-fx-background-color: transparent;")
    val scene = new Scene(box)
    scene.setFill(Color.TRANSPARENT)
    splash.initStyle(StageStyle.TRANSPARENT)
    splash.setScene(scene)
    splash.toFront()
    splash.setAlwaysOnTop(true)
    splash.setX((bounds.getMinX + bounds.getMaxX - img.getWidth) / 2)
    splash.setY((bounds.getMinY + bounds.getMaxY - img.getHeight) / 2)
  }

  override def handleApplicationNotification(preloaderNotification: Preloader.PreloaderNotification): Unit =
    preloaderNotification match {
      case progressNotification: Preloader.ProgressNotification =>
        handleProgressNotification(progressNotification)
      case stateChangeNotification: StateChangeNotification =>
        handleStateChangeNotification(stateChangeNotification)
      case _ =>
    }

  override def handleProgressNotification(progressNotification: Preloader.ProgressNotification): Unit = {
    if (progressNotification.getProgress < 1.0 && !splash.isShowing)
      splash.show()
    else if (progressNotification.getProgress >= 1.0 && splash.isShowing)
      hideSplash()
  }

  override def handleStateChangeNotification(stateChangeNotification: Preloader.StateChangeNotification): Unit = {
    if (stateChangeNotification.getType == StateChangeNotification.Type.BEFORE_START)
      hideSplash()
  }

  private def hideSplash(): Unit = {
    val ft = new FadeTransition(Duration.millis(1500), imgView)
    ft.setFromValue(1.0)
    ft.setToValue(0.0)
    ft.setOnFinished(_ => {
      splash.hide()
    })
    ft.play()
  }
}
