package tigerjython.execute

import java.awt.{AWTEvent, Toolkit}
import java.awt.event.{AWTEventListener, WindowEvent}

/**
 * We monitor AWT events to count how many windows are currently open.  This allows us to wait with shutting down an
 * execute server until all its windows are closed.  For instance, when using turtle graphics, the output window with
 * the turtle's picture will remain visible until explicitly closed by the user.
 */
object WindowMonitor extends AWTEventListener {

  private var _windowCount: Int = 0

  var onAllWindowsClosed: Runnable = _

  override def eventDispatched(event: AWTEvent): Unit = {
    event.getID match {
      case WindowEvent.WINDOW_OPENED =>
        _windowCount += 1
      case WindowEvent.WINDOW_CLOSING =>
        _windowCount -= 1
        if (_windowCount <= 0 && onAllWindowsClosed != null)
          onAllWindowsClosed.run()
      case _ =>
    }
  }

  def initialize(): Unit = {
    Toolkit.getDefaultToolkit.addAWTEventListener(this, AWTEvent.WINDOW_EVENT_MASK)
  }

  def windowCount: Int = _windowCount
}
