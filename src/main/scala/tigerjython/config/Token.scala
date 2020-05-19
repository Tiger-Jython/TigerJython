package tigerjython.config

/**
 * An individual token in the config's source.
 *
 * @author Tobias Kohn
 */
private[config]
sealed abstract class Token

private[config]
object Token {

  object ASSIGNMENT extends Token

  object DOT extends Token

  object LEFT_BRACE extends Token

  case class NAME(name: String) extends Token

  object PLUS extends Token

  object RIGHT_BRACE extends Token

  case class VALUE(value: String) extends Token
}
