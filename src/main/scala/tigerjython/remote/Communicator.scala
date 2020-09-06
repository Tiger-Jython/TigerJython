/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.remote

import java.io.{InputStream, ObjectInputStream, ObjectOutputStream, OutputStream}
import java.net.Socket

/**
 * @author Tobias Kohn
 */
abstract class Communicator {

  def inputStream: InputStream
  def outputStream: OutputStream
  def socket: Socket

  lazy val thread = new Thread(() => {
    val ois = new ObjectInputStream(inputStream)
    try {
      while (!socket.isClosed)
        handleMessage(ois.readObject().asInstanceOf[Message])
    } catch {
      case _: java.io.EOFException =>
    }
  })

  protected def handleMessage(message: Message): Unit =
    message match {
      case PingMessage(startTime) =>
        val t = System.currentTimeMillis()
        sendMessage(PongMessage(startTime, t))
      case _ =>
    }

  protected lazy val objectStream: ObjectOutputStream = new ObjectOutputStream(outputStream)

  def sendMessage(message: Message): Unit =
    if (socket != null && !socket.isClosed && message != null)
      objectStream.writeObject(message)
}