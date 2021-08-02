/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.editor

import org.fxmisc.richtext._
import tigerjython.execute.ExecLanguage
import tigerjython.files.{Document, Documents}
import tigerjython.syntaxsupport.SyntaxDocument

/**
 * This is a specialisation of the more general `EditorTab` to use the Python-editor.
 *
 * @author Tobias Kohn
 */
class PythonEditorTab extends EditorTab {

  private var _syntaxDocument: SyntaxDocument = _

  {
    caption.setValue("untitled %d".format(PythonEditorTab.nextNameIndex))
  }

  protected def createEditorNode: CodeArea = {
    val result = new PythonEditor()
    result.onAutoSave = autoSave
    _syntaxDocument = result.syntaxDocument
    result
  }

  override def getRequiredModules(target: String, execLanguage: ExecLanguage.Value): Iterable[(String, String)] = {
    _syntaxDocument.setText(getText)
    val modules = _syntaxDocument.getImportedModules
    val result = collection.mutable.ArrayBuffer[(String, String)]()
    for (mod <- modules) {
      val moduleNameParts = mod.split('.')
      if (moduleNameParts.length == 1 || moduleNameParts.length == 2)
        Documents.findDocumentWithName(moduleNameParts(0)) match {
          case Some(document) =>
            val text = document.getExecutableFileAsString(execLanguage)
            result += ((document.name.get(), text))
          case _ =>
        }
    }
    result
  }
}
object PythonEditorTab {

  private var _nameCounter: Int = 0

  private def nextNameIndex: Int = {
    val result = _nameCounter + 1
    _nameCounter = result
    result
  }

  def apply(): PythonEditorTab = new PythonEditorTab()

  def apply(document: Document): PythonEditorTab = {
    val result = new PythonEditorTab()
    result.loadDocument(document)
    result
  }
}
