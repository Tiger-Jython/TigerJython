/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.remote

import java.net.{ServerSocket, SocketException}

/**
 * The server lives in the editor itself and listens for executing instances connecting to the editor.
 *
 * This class is highly automated and you will probably only need the following fields and methods:
 * - `port`  Returns the port at which the server is listening.  Send that number to clients so they know how to
 *   connext to the server.
 * - `clients(id)`  Returns the client associated with a given ID, if the client is up and running and has been
 *   registered with the server (clients register with the server automatically as soon as they have been started).
 * - `onNewClient`  Lets you set a function to be called whenever a new client registers with the server.
 *
 * @author Tobias Kohn
 */
object ExecuteServer {

  private val _clients = collection.mutable.Map[Int, ExecuteClientProxy]()

  private var _serverSocket: ServerSocket = _
  private var _port: Int = 0

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
  }

  protected[remote]
  def addClient(client: ExecuteClientProxy): Unit =
    if (client != null && client.id > 0) {
      _clients(client.id) = client
    }

  protected[remote]
  def removeClient(client: ExecuteClientProxy): Unit =
    _clients.remove(client.id)
}