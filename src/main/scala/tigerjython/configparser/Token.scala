/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.configparser

/**
 * An individual token in the config's source.
 *
 * @author Tobias Kohn
 */
private[configparser]
sealed abstract class Token

private[configparser]
object Token {

  object ASSIGNMENT extends Token

  object DOT extends Token

  object LEFT_BRACE extends Token

  case class NAME(name: String) extends Token

  object PLUS extends Token

  object RIGHT_BRACE extends Token

  case class VALUE(value: String) extends Token
}
