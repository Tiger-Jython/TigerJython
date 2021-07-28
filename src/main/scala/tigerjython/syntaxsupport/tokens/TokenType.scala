package tigerjython.syntaxsupport.tokens

/**
 * @author Tobias Kohn
 */
object TokenType extends Enumeration {

  final val NONE          = Value("<NONE>")
  final val COMMENT       = Value("<COMMENT>")
  final val INVALID       = Value("<INVALID>")
  final val NEWLINE       = Value("<NEWLINE>")
  final val WHITESPACE    = Value("<WHITESPACE>")

  final val ASSIGNMENT    = Value("assignment")
  final val ATTRIBUTE     = Value("attribute")
  final val BUILTIN_NAME  = Value("builtin-name")
  final val CLASS_KEYWORD = Value("class-keyword")
  final val COLON         = Value("colon")
  final val COMMA         = Value("comma")
  final val COMPARATOR    = Value("comparator")
  final val DEF_KEYWORD   = Value("def-keyword")
  final val DELIMITER     = Value("delimiter")
  final val DOT           = Value("dot")
  final val ELLIPSIS      = Value("ellipsis")
  final val EXPR_ASSIGN   = Value("expr-assignment")
  final val KEYWORD       = Value("keyword")
  final val LAMBDA        = Value("lambda")
  final val LEFT_BRACKET  = Value("left-bracket")
  final val NAME          = Value("name")
  final val NUMBER        = Value("number")
  final val RIGHT_BRACKET = Value("right-bracket")
  final val STRING_LITERAL= Value("string")
  final val SYMBOL        = Value("symbol")
}
