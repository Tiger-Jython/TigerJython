/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.remote

/**
 * @author Tobias Kohn
 */
sealed abstract class Message extends Serializable {
}

case class ErrorResultMessage(error: String, tag: Int) extends Message

case class EvalMessage(script: String, tag: Int) extends Message

case class ExecMessage(script: String, tag: Int) extends Message

case class ExecFileMessage(filename: String, tag: Int) extends Message

case class IDMessage(id: Int) extends Message

case class PingMessage(startTime: Long) extends Message

case class ProgramDoneMessage(tag: Int) extends Message

case class PongMessage(startTime: Long, echoTime: Long) extends Message

case class QuitMessage(forceQuit: Boolean) extends Message

case class ResultMessage(result: String, tag: Int) extends Message

case class SystemInfoMessage(javaVersion: Int, CPUCount: Int) extends Message

case class SystemStatusMessage(time: Long, threadCount: Int, memoryUsed: Long, memoryMax: Long) extends Message
