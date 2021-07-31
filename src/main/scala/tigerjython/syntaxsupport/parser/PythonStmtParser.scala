/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.parser

import tigerjython.syntaxsupport.SyntaxDocument
import tigerjython.syntaxsupport.struct.StructElement
import tigerjython.syntaxsupport.tokens._

/**
 * @author Tobias Kohn
 */
class PythonStmtParser(val document: SyntaxDocument) extends StmtParser {

  private var _lambdaNames = collection.mutable.ArrayBuffer[String]()
  private var _nameType: NameTokenType.Value = NameTokenType.UNKNOWN
  private var _source: TokenSource = _

  protected def source: TokenSource = _source

  def parse(source: TokenSource): StatementType =
    if (source != null) {
      _source = source
      parseStmt()
    } else
      null

  def parse(structElement: StructElement): StatementType =
    if (structElement != null)
      parse(new TokenSource(document.tokens, structElement))
    else
      null

  protected def parseStmt(): StatementType =
    source.next() match {
      case null =>
        null
      case Token(TokenType.CLASS_KEYWORD, _) =>
        parseClass()
      case Token(TokenType.DEF_KEYWORD, _) =>
        parseDef()
      case NameToken.Keyword("async") =>
        parseStmt()
      case NameToken.Keyword("del") =>
        parseDel()
      case NameToken.Keyword("for") =>
        parseFor()
      case NameToken.Keyword("global" | "nonlocal") =>
        parseGlobal()
      case NameToken.Keyword("with") =>
        parseWith()
      case NameToken.Keyword("from") =>
        parseImportFrom()
      case NameToken.Keyword("import") =>
        parseImport()
      case NameToken.Keyword("return") =>
        parseReturn()
      case NameToken.Keyword("raise") =>
        parseRaise()
      case NameToken.Keyword("assert") =>
        parseAssert()
      case NameToken.Keyword("while") =>
        parseWhile()
      case NameToken.Keyword("if") =>
        parseIf()
      case NameToken.Keyword("else") =>
        parseElse()
      case NameToken.Keyword("elif") =>
        parseElif()
      case NameToken.Keyword("try") =>
        parseTry()
      case NameToken.Keyword("except") =>
        parseElif()
      case NameToken.Keyword("finally") =>
        parseFinally()
      case NameToken.Keyword("break") =>
        StatementType.BREAK
      case NameToken.Keyword("continue") =>
        StatementType.CONTINUE
      case NameToken.Keyword("pass") =>
        StatementType.PASS
      case NameToken.Keyword(_) =>
        parseExpr()
        StatementType.UNKNOWN
      case _ =>
        source.back()
        parseSimpleStmtList()
    }

  protected def parseAssert(): StatementType = {
    parseExpr()
    StatementType.ASSERT
  }

  protected def parseClass(): StatementType = {
    val name = source.expectName(NameTokenType.DEF_NAME)
    if (source.expect(TokenType.LEFT_BRACKET)) {
      parseCallArguments()
      source.expectRightBracket()
      if (source.expectCompoundColon())
        parseSimpleStmt()
    }
    StatementType.CLASS_DEF(name)
  }

  protected def parseDef(): StatementType = {
    val name = source.expectName(NameTokenType.DEF_NAME)
    if (source.expect(TokenType.LEFT_BRACKET)) {
      parseParams()
      source.expectRightBracket()
      if (source.expectSymbol("->"))
        parseTypeAnnotation()
      source.expectCompoundColon()
      StatementType.FUNC_DEF(name)
    } else
      StatementType.FUNC_DEF(name)
  }

  protected def parseDel(): StatementType = {
    _nameType = NameTokenType.DEL_TARGET
    while (source.hasNext) {
      readExtName()
      source.expectComma()
    }
    StatementType.DEL
  }

  protected def parseElif(): StatementType = {
    parseExpr()
    if (source.expectCompoundColon())
      parseSimpleStmt()
    StatementType.ELIF
  }

  protected def parseElse(): StatementType = {
    if (source.expectCompoundColon())
      parseSimpleStmt()
    StatementType.ELSE
  }

  protected def parseExcept(): StatementType = {
    _nameType = NameTokenType.LOAD
    parseExpr()
    if (source.expectKeyword("as"))
      source.next() match {
        case name: NameToken if name.isName =>
          name.nameTokenType = NameTokenType.STORE
        case _ =>
      }
    if (source.expectCompoundColon())
      parseSimpleStmt()
    StatementType.EXCEPT
  }

  protected def parseFinally(): StatementType = {
    if (source.expectCompoundColon())
      parseSimpleStmt()
    StatementType.FINALLY
  }

  protected def parseFor(): StatementType = {
    _nameType = NameTokenType.STORE
    parseTargetList()
    source.expectKeyword("in")
    _nameType = NameTokenType.LOAD
    parseExpr()
    if (source.expectCompoundColon())
      parseSimpleStmt()
    StatementType.FOR
  }

  protected def parseGlobal(): StatementType = {
    StatementType.GLOBAL(Array())
  }

  protected def parseIf(): StatementType = {
    _nameType = NameTokenType.LOAD
    parseExpr()
    if (source.expectCompoundColon())
      parseSimpleStmt()
    StatementType.IF
  }

  protected def parseImport(): StatementType = {
    val modules = collection.mutable.ArrayBuffer[String]()
    var mod = readImportModuleAlias()
    while (mod != null && source.expectComma()) {
      modules += mod
      mod = readImportModuleAlias()
    }
    if (mod != null)
      modules += mod
    StatementType.IMPORT(modules.toArray)
  }

  protected def parseImportFrom(): StatementType = {
    val moduleName = readImportModule()
    if (source.expectKeyword("import")) {
      var a = readAlias()
      while (a != null && source.expectComma())
        a = readAlias()
    }
    StatementType.IMPORT(Array(moduleName))
  }

  protected def parseRaise(): StatementType = {
    parseExpr()
    if (source.expectKeyword("from"))
      parseExpr()
    StatementType.RAISE
  }

  protected def parseReturn(): StatementType = {
    _nameType = NameTokenType.LOAD
    parseExprList()
    StatementType.RETURN
  }

  protected def parseSimpleStmt(): Unit =
    source.headTokenType match {
      case TokenType.NAME | TokenType.BUILTIN_NAME | TokenType.LEFT_BRACKET =>
        _nameType = NameTokenType.UNKNOWN
        parseTargetList()
      case _ =>
        _nameType = NameTokenType.LOAD
        parseExprList()
    }

  protected def parseSimpleStmtList(): StatementType = {
    parseSimpleStmt()
    if (source.expectSymbol(";"))
      parseSimpleStmt()
    StatementType.UNKNOWN
  }

  protected def parseTry(): StatementType = {
    if (source.expectCompoundColon())
      parseSimpleStmt()
    StatementType.TRY
  }

  protected def parseWhile(): StatementType = {
    _nameType = NameTokenType.LOAD
    parseExpr()
    if (source.expectCompoundColon())
      parseSimpleStmt()
    StatementType.WHILE
  }

  protected def parseWith(): StatementType = {
    parseWithItemList()
    if (source.expectCompoundColon())
      parseSimpleStmt()
    StatementType.WITH
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected def parseCallArguments(): Unit =
    if (source.expectBracket('('))
      while (source.hasNext && !source.expectBracket(')')) {
        source.head match {
          case n: NameToken if source.peek(1) == "=" =>
            n.nameTokenType = NameTokenType.ARG_KEYWORD
            source.skip(2)
          case _ =>
        }
        parseExpr()
        if (source.expectKeyword("for"))
          parseGenerator()
        if (!source.expectComma())
          source.ignoreAllUntil(TokenType.RIGHT_BRACKET)
      }

  protected def parseDictItem(): Unit = {
    parseExpr()
    if (source.expectColon())
      parseExpr()
  }

  protected def parseDictList(): Unit = {
    parseDictItem()
    if (source.expectComma())
      parseDictItem()
  }

  protected def parseExpr(): Unit =
    if (source.headTokenType == TokenType.KEYWORD)
      source.head match {
        case NameToken(TokenType.KEYWORD, _, "yield") =>
          source.skip()
          source.expectKeyword("from")
          parseExpr()
        case _ =>
      }
    else if (source.headTokenType == TokenType.LAMBDA)
      parseLambda()
    else
      while (source.hasNext)
        source.headTokenType match {
          case TokenType.NAME | TokenType.BUILTIN_NAME =>
            readExtName() match {
              case Some(name) =>
                if (name.nameTokenType == NameTokenType.UNKNOWN)
                  name.nameTokenType = _nameType
              case None =>
            }
          case TokenType.LEFT_BRACKET =>
            // list, tuple, dict, ...
            source.next() match {
              case left @ LeftBracketToken('(' | '[') =>
                left._isData = true
                parseExpr()
                if (source.expectKeyword("for"))
                  parseGenerator()
                else if (source.expectComma())
                  parseExprList()
                else if (left.bracket == '(' && !(source.prev eq left))
                  left._isData = false
                source.expectRightBracket(left)
              case left @ LeftBracketToken('{') =>
                parseDictItem()
                if (source.expectKeyword("for"))
                  parseGenerator()
                else if (source.expectComma())
                  parseDictList()
                source.expectRightBracket(left)
            }
          case TokenType.RIGHT_BRACKET | TokenType.DELIMITER | TokenType.COLON | TokenType.COMMA |
               TokenType.DEF_KEYWORD | TokenType.CLASS_KEYWORD | TokenType.ASSIGNMENT =>
            return
          case TokenType.KEYWORD =>
            if (source.expectKeyword("if")) {
              parseExpr()
              if (source.expectKeyword("else"))
                parseExpr()
            }
            else if (source.expectKeyword("for"))
              parseGenerator()
            return
          case _ =>
            source.skip()
        }

  protected def parseExprList(): Unit = {
    parseExpr()
    if (source.expectComma())
      parseExprList()
  }

  protected def parseGenerator(): Unit = {
    _nameType = NameTokenType.LOCAL_STORE
    parseTargetList()
    source.expectKeyword("in")
    _nameType = NameTokenType.LOAD
    parseExpr()
    while (source.expectKeyword("if"))
      parseExpr()
    if (source.expectKeyword("for"))
      parseGenerator()
  }

  protected def parseLambda(): Unit =
    if (source.expect(TokenType.LAMBDA)) {
      val args = parseParams(isLambda = true)
      if (source.expectColon()) {
        val idx = _lambdaNames.length
        _lambdaNames.addAll(args)
        parseExpr()
        _lambdaNames.remove(idx, _lambdaNames.length - idx)
      }
    }

  protected def parseParams(isLambda: Boolean = false): List[String] = {
    val nameTokenType = if (isLambda) NameTokenType.LAMBDA_PARAM else NameTokenType.FORMAL_ARG
    val isStarred = source.expectStar()
    source.expectStar()
    val arg = source.expectName(nameTokenType)
    if (arg != null && !isStarred) {
      if (source.expect(TokenType.COLON))
        parseTypeAnnotation()
      if (source.expectSymbol("="))
        parseExpr()
      if (source.expectComma())
        arg :: parseParams()
      else
        arg :: Nil
    } else {
      source.expectSymbol("/")
      if (source.expectComma())
        parseParams()
      else
        Nil
    }
  }

  protected def parseSlice(): Unit = {
    parseExpr()
    if (source.expect(TokenType.COLON)) {
      parseExpr()
      if (source.expect(TokenType.COLON))
        parseExpr()
    }
  }

  protected def parseSliceList(): Unit = {
    parseExpr()
    if (source.expectComma())
      parseSliceList()
  }

  protected def parseSubscript(): Unit =
    if (source.expectBracket('[')) {
      parseSliceList()
      source.expectBracket(']')
    }

  protected def parseTargetList(): Unit = {
    _nameType = NameTokenType.UNKNOWN
    val names = readTargetList()
    if (source.expect(TokenType.ASSIGNMENT)) {
      for (name <- names)
        if (name.nameTokenType == NameTokenType.UNKNOWN)
          name.nameTokenType = NameTokenType.ASSIGN_TARGET
    } else {
      for (name <- names)
        if (name.nameTokenType == NameTokenType.UNKNOWN)
          name.nameTokenType = NameTokenType.LOAD
    }
    _nameType = NameTokenType.LOAD
    parseExprList()
  }

  protected def parseTypeAnnotation(): Unit = {
    val startIndex = source.index
    parseExpr()
    source.markAsAnnotation(startIndex, source.index)
  }

  protected def parseWithItemList(): Unit = {
    parseExpr()
    if (source.expectKeyword("as")) {
      _nameType = NameTokenType.STORE
      readExtName()
    }
    if (source.expectComma())
      parseWithItemList()
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private def readAlias(): String =
    source.next() match {
      case n @ NameToken(TokenType.NAME | TokenType.BUILTIN_NAME, _, s) =>
        if (source.expectKeyword("as")) {
          n.nameTokenType = NameTokenType.LOAD
          source.expectName(NameTokenType.STORE)
        } else {
          n.nameTokenType = NameTokenType.STORE
          s
        }
      case _ =>
        null
    }

  private def readAttr(): Unit =
    while (source.expectSymbol("."))
      source.next() match {
        case name: NameToken if name.tokenType == TokenType.NAME || name.tokenType == TokenType.BUILTIN_NAME =>
          name.nameTokenType = NameTokenType.ATTRIBUTE
        case _ =>
          source.back()
      }

  private def readExtName(): Option[NameToken] =
    source.next() match {
      case name: NameToken if name.isName =>
        source.head match {
          case LeftBracketToken('(') =>
            name.nameTokenType = NameTokenType.CALL
            parseCallArguments()
          case LeftBracketToken('[') =>
            name.nameTokenType = NameTokenType.SUBSCRIPT
            parseSubscript()
          case SymbolToken(TokenType.DOT, _) =>
            if (_lambdaNames.contains(name.text))
              name.nameTokenType = NameTokenType.LAMBDA_ARG
            else
              name.nameTokenType = NameTokenType.LOAD
            readAttr()
          case Token(TokenType.EXPR_ASSIGN, _) =>
            name.nameTokenType = NameTokenType.STORE
          case _ =>
            if (_lambdaNames.contains(name.text))
              name.nameTokenType = NameTokenType.LAMBDA_ARG
            else
              name.nameTokenType = _nameType
        }
        Some(name)
      case _ =>
        None
    }

  private def readImportModule(): String = {
    val moduleName = new StringBuilder()
    while (source.hasNext)
      source.next() match {
        case n @ NameToken(TokenType.NAME | TokenType.BUILTIN_NAME, _, s) =>
          moduleName.append(s)
          n.nameTokenType = NameTokenType.MODULE
        case SymbolToken(TokenType.DOT | TokenType.ELLIPSIS, s) =>
          moduleName.append(s)
        case _: WhitespaceToken =>
        case _ =>
          source.back()
          return moduleName.toString()
      }
    moduleName.toString()
  }

  private def readImportModuleAlias(): String = {
    val module = readImportModule()
    if (module != "") {
      source.prev match {
        case n: NameToken =>
          n.nameTokenType = NameTokenType.LOAD
        case _ =>
      }
      if (source.expectKeyword("as"))
        source.expectName(NameTokenType.STORE)
      module
    } else
      null
  }

  private def readTargetList(): List[NameToken] =
    source.head match {
      case LeftBracketToken('(') =>
        source.skip()
        val result = readTargetList()
        source.expectBracket(')')
        result
      case LeftBracketToken('[') =>
        source.skip()
        val result = readTargetList()
        source.expectBracket(']')
        result
      case Token(TokenType.NAME | TokenType.BUILTIN_NAME, _) =>
        readExtName() match {
          case Some(name) =>
            if (source.expectComma())
              name :: readTargetList()
            else
              name :: Nil
          case None =>
            Nil
        }
      case _ =>
        Nil
    }
}
