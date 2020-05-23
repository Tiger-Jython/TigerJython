/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.plugins

import java.util.concurrent.ArrayBlockingQueue

/**
 * Some plugins might want to monitor events such as when a program is executed, stopped, or an error occurs.  This
 * objects provides the means to register listener to various events.
 *
 * Note that all events are merely notification events that are run in a separate thread.  They are intended to monitor
 * user actions, not to actually react to them.
 *
 * @author Tobias Kohn
 */
object EventManager {

  private sealed abstract class NotificationEvent
  private case class ErrorNotificationEvent(time: Long, line: Int, column: Int, msg: String) extends NotificationEvent
  private case class KeyPressNotificationEvent(time: Long, pos: Int, key: String) extends NotificationEvent
  private case class RunningStateNotificationEvent(listeners: Iterable[TimedNotification], time: Long) extends NotificationEvent

  private val queue = new ArrayBlockingQueue[NotificationEvent](256)

  lazy val notificationThread: Thread = {
    val result = new Thread(() => {
      while (true) {
        while (!queue.isEmpty) {
          queue.poll() match {
            case ErrorNotificationEvent(time, line, column, msg) =>
              for (f <- onError)
                f.apply(time, line, column, msg)
            case KeyPressNotificationEvent(time, pos, key) =>
              for (f <- onKeyPressed)
                f.apply(time, pos, key)
            case RunningStateNotificationEvent(listeners, time) =>
              for (f <- listeners)
                f.apply(time)
          }
        }
        Thread.sleep(10)
      }
    })
    result.setDaemon(true)
    result.start()
    result
  }

  /**
   * An `onError` event is fired when an error is reported to the user.  This can either be a syntax error, or a
   * runtime exception.
   */
  val onError: collection.mutable.Set[ErrorNotification] = collection.mutable.Set[ErrorNotification]()

  /**
   * The `onKeyPressed` events are fired for "character" keys, i.e. those that modify the text.
   */
  val onKeyPressed: collection.mutable.Set[KeyPressNotification] = collection.mutable.Set[KeyPressNotification]()

  /**
   * When the user clicks `run` to execute the current Python program, an `onRun` event is fired.  This happens before
   * any syntax checking is done, or the script is actually executed.
   */
  val onRun: collection.mutable.Set[TimedNotification] = collection.mutable.Set[TimedNotification]()

  /**
   * As soon as the script has started, the `onStarted` event is fired.  At this point, the script is probably running
   * (depending on timing).
   */
  val onStarted: collection.mutable.Set[TimedNotification] = collection.mutable.Set[TimedNotification]()

  /**
   * As soon as the script has stopped running --- either because it has naturally stopped or because of an error ---
   * the `onStopped` event is fired.
   */
  val onStopped: collection.mutable.Set[TimedNotification] = collection.mutable.Set[TimedNotification]()

  protected[tigerjython]
  def fireOnError(line: Int, column: Int, msg: String): Unit =
    if (onError.nonEmpty)
      queue.offer(ErrorNotificationEvent(System.currentTimeMillis(), line, column, msg))

  protected[tigerjython]
  def fireOnKeyPressed(pos: Int, key: String): Unit =
    queue.offer(KeyPressNotificationEvent(System.currentTimeMillis(), pos, key))

  protected[tigerjython]
  def fireOnRun(): Unit =
    if (onRun.nonEmpty)
      queue.offer(RunningStateNotificationEvent(onRun, System.currentTimeMillis()))

  protected[tigerjython]
  def fireOnStarted(): Unit =
    if (onStarted.nonEmpty)
      queue.offer(RunningStateNotificationEvent(onStarted, System.currentTimeMillis()))

  protected[tigerjython]
  def fireOnStopped(): Unit =
    if (onStopped.nonEmpty)
      queue.offer(RunningStateNotificationEvent(onStopped, System.currentTimeMillis()))

  def addOnErrorListener(listener: ErrorNotification): Unit =
    if (listener != null) {
      onError.add(listener)
      notificationThread
    }

  def addOnKeyPressedListener(listener: KeyPressNotification): Unit =
    if (listener != null) {
      onKeyPressed.add(listener)
      notificationThread
    }

  def addOnRunListener(listener: TimedNotification): Unit =
    if (listener != null) {
      onRun.add(listener)
      notificationThread
    }

  def addOnStartedListener(listener: TimedNotification): Unit =
    if (listener != null) {
      onStarted.add(listener)
      notificationThread
    }

  def addOnStoppedListener(listener: TimedNotification): Unit =
    if (listener != null) {
      onStopped(listener)
      notificationThread
    }

  def removeOnErrorListener(listener: ErrorNotification): Unit =
    onError.remove(listener)

  def removeOnKeyPressedListener(listener: KeyPressNotification): Unit =
    onKeyPressed.remove(listener)

  def removeOnRunListener(listener: TimedNotification): Unit =
    onRun.remove(listener)

  def removeOnStartedListener(listener: TimedNotification): Unit =
    onStarted.remove(listener)

  def removeOnStoppedListener(listener: TimedNotification): Unit =
    onStopped.remove(listener)
}
