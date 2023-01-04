package tigerjython.ui

import javafx.scene.control.{ContextMenu, Hyperlink, MenuItem, Tooltip}
import javafx.scene.input.{Clipboard, ClipboardContent}

import java.net.URI

/**
 * This provides a clickable weblink that will open the specified destination in the system's browser.
 */
class Weblink(val caption: String, val destination: URI) extends Hyperlink {

  def this(caption: String, destination: String) = this(caption, new URI(destination))

  this.setText(caption)
  this.setOnAction(_ => { open() })
  this.setTooltip(new Tooltip(getTooltipText))
  this.setContextMenu(menu)

  private def getTooltipText: String = {
    val d = destination.toString
    if (d.startsWith("mailto:"))
      d.drop(7)
    else
      d
  }

  private def open(): Unit = {
    TigerJythonApplication.currentApplication.getHostServices.showDocument(destination.toString)
  }

  private lazy val menu: ContextMenu = {
    val menu = new ContextMenu()
    val openAction = new MenuItem("open")
    val copyAction = new MenuItem("copy")
    UIString("open.caption") += openAction.textProperty
    UIString("copy.caption") += copyAction.textProperty
    openAction.setOnAction(_ => { open() })
    copyAction.setOnAction(_ => {
      val clp = Clipboard.getSystemClipboard
      val ctx = new ClipboardContent()
      ctx.putString(getTooltipText)
      clp.setContent(ctx)
    })
    menu.getItems.addAll(openAction, copyAction)
    menu
  }
}
