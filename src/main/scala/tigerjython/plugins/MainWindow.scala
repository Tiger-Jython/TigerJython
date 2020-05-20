package tigerjython.plugins

import javafx.scene.control.{Alert, ButtonType}
import javafx.scene.control.Alert.AlertType
import tigerjython.ui.MenuManager

/**
 * The `MainWindow` class is actually only an interface to allow plugins to access the general user interface.
 *
 * @author Tobias Kohn
 */
class MainWindow(val menuManager: MenuManager) {

  def addMenuItem(name: String, caption: String, action: Runnable): Unit =
    menuManager.addMenuItem(name, caption, action)

  def alert(message: String): Unit = {
    val alert = new Alert(AlertType.NONE, message, ButtonType.OK)
    alert.showAndWait()
  }
}
