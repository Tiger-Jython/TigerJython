/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.notebook.json

/**
 * Represents a cell in a Jupyter notebook.
 */
abstract class JupyterCell
abstract class CellOutput

case class CodeCell(text: String, outputs: Array[CellOutput]) extends JupyterCell
case class MarkdownCell(text: String) extends JupyterCell
case class RawTextCell(text: String) extends JupyterCell

case class ErrorOutput(name: String, value: String, traceback: String) extends CellOutput
case class TextOutput(text: String) extends CellOutput
