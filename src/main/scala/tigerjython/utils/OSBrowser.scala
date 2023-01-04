package tigerjython.utils

import java.net.{URI, URL}

/**
 * Use this class to open a link in the system's default browser.
 *
 * Using the 'standard' approach via Java's `awt.Desktop` just crashes the application.
 */
object OSBrowser {

  private def run(command: String): Unit = {
    val t = new Thread(() => {
      val rt = Runtime.getRuntime
      rt.exec(command)
    })
    t.setDaemon(true)
    t.start()
  }

  private def run(command: String, arg: String): Unit = {
    new Thread(() => {
      val rt = Runtime.getRuntime
      rt.exec(Array(command, arg))
    }).start()
  }

  def browse(url: URI): Unit = {
    OSPlatform.system match {
      case OSPlatform.WINDOWS =>
        run("rundll32 url.dll,FileProtocolHandler " + url.toString)
      case OSPlatform.MAC_OS =>
        run("open", url.toString)
      case OSPlatform.LINUX =>
        run("xdg-open", url.toString)
      case _ =>
    }
  }

  def browse(url: URL): Unit =
    browse(url.toURI)

  def browse(url: String): Unit =
    browse(URI.create(url))
}
