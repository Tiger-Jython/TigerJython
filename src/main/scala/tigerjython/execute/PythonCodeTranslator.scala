package tigerjython.execute

import tigerjython.core.Preferences
import tigerpython.parser.errors.ErrorHandler
import tigerpython.parser.lexer.{Lexer, TokenType}
import tigerpython.parser.parsing.ParserState

/**
 * In order to support some syntax enhancements such as `repeat` loops, we need to replace some tokens in the source
 * code by others to create a fully compatible script.  This object is the interface for such code transformations.
 *
 * @author Tobias Kohn
 */
object PythonCodeTranslator {

  private val REPEAT_STRING = Map[Int, String](
    2 -> "for _tj_repeat_counter_ in xrange(long(%s))",
    3 -> "for _tj_repeat_counter in range(int(%s))"
  )

  /**
   * Takes a Python program as input and transforms it so as to build a fully compatible script.
   *
   * If the code is transformed, the new program is returned.  If the code requires no changes, the function returns
   * `None`.
   *
   * @param code  The source code as a `String` value.
   * @return      Either `None` or a new `String` representing the transformed code.
   */
  def translate(code: String): Option[String] = {
    val version = PythonInstallations.getSelectedVersionNumber
    if (Preferences.repeatLoop.get)
      _translate(code, version)
    else
      None
  }

  protected def _translate(code: String, pythonVersion: Int): Option[String] = {
    val parserState = ParserState(code, pythonVersion, ErrorHandler.SilentErrorHandler)
    parserState.repeatStatement = Preferences.repeatLoop.get
    val lexer = new Lexer(code, parserState, 0)
    val result = new StringBuilder()
    var position: Int = 0
    for (token <- lexer)
      token.tokenType match {
        case TokenType.REPEAT =>
          result ++= code.substring(position, token.pos)
          val n = lexer.head
          if (n != null && n.tokenType == TokenType.COLON) {
            result ++= "while True"
            position = token.endPos
          } else
            if (n != null) {
              position = n.pos
              var bracketLevel: Int = 0
              while (lexer.hasNext && !(bracketLevel == 0 && lexer.head.tokenType == TokenType.COLON))
                lexer.next.tokenType match {
                  case TokenType.LEFT_BRACE | TokenType.LEFT_BRACKET | TokenType.LEFT_PARENS =>
                    bracketLevel += 1
                  case TokenType.RIGHT_BRACE | TokenType.RIGHT_BRACKET | TokenType.RIGHT_PARENS =>
                    bracketLevel -= 1
                  case _ =>
                }
              val hd = lexer.head
              if (hd != null) {
                val argument = code.substring(position, hd.pos)
                result ++= REPEAT_STRING(pythonVersion).format(argument)
                position = hd.pos
              }
            } else
              position = token.endPos
        case _ =>
      }
    if (position > 0) {
      result ++= code.substring(position)
      Some(result.toString)
    } else
      None
  }
}
