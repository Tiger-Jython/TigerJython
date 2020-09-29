/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.remote

import java.net.{ServerSocket, SocketException}

import tigerjython.core.Configuration

/**
 * The server lives in the editor itself and listens for executing instances connecting to the editor.
 *
 * This class is highly automated and you will probably only need the following fields and methods:
 * - `port`  Returns the port at which the server is listening.  Send that number to clients so they know how to
 *   connect to the server.
 * - `clients(id)`  Returns the client associated with a given ID, if the client is up and running and has been
 *   registered with the server (clients register with the server automatically as soon as they have been started).
 * - `onNewClient`  Lets you set a function to be called whenever a new client registers with the server.
 *
 * @author Tobias Kohn
 */
object ExecuteServer {

  private val _clients = collection.mutable.Map[Int, ExecuteClientProxy]()

  private var _serverSocket: ServerSocket = _
  private var _port: Int = Configuration.getDefaultPort

  private val waitingRoom = collection.mutable.Map[Int, ExecuteClientProxy=>Unit]()

  def serverSocket: ServerSocket = _serverSocket
  def port: Int = _port

  var onNewClient: ExecuteClientProxy=>Unit = _

  private val thread = new Thread(() => {
    try {
      while (!serverSocket.isClosed) {
        val socket = serverSocket.accept()
        val client = new ExecuteClientProxy(socket)
        if (onNewClient != null)
          onNewClient(client)
      }
    } catch {
      case _: SocketException =>
    }
  })

  def apply(id: Int): Option[ExecuteClientProxy] =
    _clients.get(id)

  def client(id: Int): ExecuteClientProxy =
    _clients.get(id).orNull

  def getPort: Int = {
    if (_serverSocket == null)
      initialize()
    _port
  }

  def initialize(): Unit = {
    _serverSocket = new ServerSocket(0)
    _port = serverSocket.getLocalPort
    thread.setDaemon(true)
    thread.start()
  }

  def quit(): Unit = {
    _serverSocket.close()
    for ((_, c) <- _clients)
      c.close()
  }

  protected[remote]
  def addClient(client: ExecuteClientProxy): Unit =
    if (client != null && client.id > 0) {
      _clients(client.id) = client
      handleWaitingRoom(client)
    }

  protected[remote]
  def updateClientId(oldId: Int, client: ExecuteClientProxy): Unit = {
    _clients.remove(oldId)
    addClient(client)
  }

  def updateClientId(oldId: Int, newId: Int): Unit =
    _clients.get(oldId) match {
      case Some(proxy) =>
        proxy.changeId(newId)
      case _ =>
    }

  protected[remote]
  def removeClient(client: ExecuteClientProxy): Unit =
    _clients.remove(client.id)

  protected def handleWaitingRoom(proxy: ExecuteClientProxy): Unit =
    synchronized {
      waitingRoom.remove(proxy.id) match {
        case Some(onReady) =>
          onReady(proxy)
        case _ =>
      }
    }

  def waitForProxy(id: Int, onReady: ExecuteClientProxy=>Unit): Unit =
    synchronized {
      _clients.get(id) match {
        case Some(proxy) =>
          onReady(proxy)
        case None =>
          waitingRoom(id) = onReady
      }
    }
}
