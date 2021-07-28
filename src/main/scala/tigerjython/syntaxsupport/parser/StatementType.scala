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
sealed abstract class StatementType

object StatementType {

  abstract class CompoundStmt extends StatementType
  abstract class DefinitionStmt extends CompoundStmt

  case object UNKNOWN extends StatementType
  case object ASSERT extends StatementType
  case object BREAK extends StatementType
  case object CONTINUE extends StatementType
  case object DEL extends StatementType
  case object ELIF extends CompoundStmt
  case object ELSE extends CompoundStmt
  case object EXCEPT extends CompoundStmt
  case object FINALLY extends CompoundStmt
  case object FOR extends CompoundStmt
  case object IF extends CompoundStmt
  case object PASS extends StatementType
  case object RAISE extends StatementType
  case object REPEAT extends CompoundStmt
  case object RETURN extends StatementType
  case object TRY extends CompoundStmt
  case object WHILE extends CompoundStmt
  case object WITH extends CompoundStmt

  case class CLASS_DEF(name: String) extends DefinitionStmt
  case class FUNC_DEF(name: String) extends DefinitionStmt
  case class GLOBAL(names: Array[String]) extends StatementType
  case class IMPORT(modules: Array[String]) extends StatementType
  case class NONLOCAL(names: Array[String]) extends StatementType
}
