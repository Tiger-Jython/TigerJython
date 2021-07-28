/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.syntaxsupport.parser

/**
 * @author Tobias Kohn
 */
abstract class AstStmtNode extends AstNode {

  def assignTargets: Array[String]
}

object AstStmtNode {

  case class Assignment(assignTargets: Array[String]) extends AstStmtNode

  case class ClassDef(name: String) extends AstStmtNode {

    override def assignTargets: Array[String] = Array(name)
  }

  case class For(assignTargets: Array[String]) extends AstStmtNode

  case class FunctionDef(name: String, args: Array[String]) extends AstStmtNode {

    override def assignTargets: Array[String] = Array(name)
  }

  case class With(assignTargets: Array[String]) extends AstStmtNode
}