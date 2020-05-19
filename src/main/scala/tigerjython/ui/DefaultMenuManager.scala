/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui

import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.Node
import javafx.scene.control._
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import org.fxmisc.richtext.StyleClassedTextArea
import tigerjython.ui.editor.EditorTab

/**
 * This is the standard implementation of the menu manager and is used to build up the standard menu.
 *
 * @author Tobias Kohn
 */
class DefaultMenuManager(val application: TigerJythonApplication) extends MenuManager {

  import tigerjython.ui.Utils._

  def item(uiText: String, onAction: EventHandler[ActionEvent]): MenuItem = {
    val result = new MenuItem
    setUICaption(uiText, result.textProperty())
    result.setOnAction(onAction)
    result
  }

  protected lazy val cutItem: MenuItem = item("cut", _ => cut())

  protected lazy val copyItem: MenuItem = item("copy", _ => copy())

  protected lazy val pasteItem: MenuItem = item("paste", _ => paste())

  def focusChanged(node: Node): Unit =
    node match {
      case _: TextArea | _: StyleClassedTextArea =>
        enableClipboardOperations(true)
      case _ =>
        enableClipboardOperations(false)
    }

  def copy(): Unit =
    application.getFocusedControl match {
      case textArea: TextArea =>
        onFX(() => { textArea.copy() })
      case textArea: StyleClassedTextArea =>
        onFX(() => { textArea.copy() })
      case _ =>
    }

  def cut(): Unit =
    application.getFocusedControl match {
      case textArea: TextArea =>
        onFX(() => { textArea.cut() })
      case textArea: StyleClassedTextArea =>
        onFX(() => { textArea.cut() })
      case _ =>
    }

  private var _clipboardOperationsEnabled: Boolean = true

  protected def enableClipboardOperations(enable: Boolean): Unit =
    if (enable != _clipboardOperationsEnabled)
      onFX(() => {
        copyItem.setDisable(!enable)
        cutItem.setDisable(!enable)
        _clipboardOperationsEnabled = enable
      })

  def paste(): Unit =
    application.getFocusedControl match {
      case textArea: TextArea =>
        onFX(() => { textArea.paste() })
      case textArea: StyleClassedTextArea =>
        onFX(() => { textArea.paste() })
      case _ =>
    }

  protected lazy val fileChooser: FileChooser = {
    val result = new FileChooser()
    result.getExtensionFilters.addAll(
      new ExtensionFilter("Python Files", "*.py"),
      new ExtensionFilter("All Files", "*.*")
    )
    result
  }

  def newFile(): Unit = {
    application.tabManager.addTab(editor.PythonEditorTab())
  }

  private def getEmptyTextFile: Option[EditorTab] =
    application.tabManager.currentFrame match {
      case Some(editorTab: EditorTab) if editorTab.isEmpty =>
        Some(editorTab)
      case _ =>
        None
    }

  def openFile(): Unit = {
    val selectedFile = fileChooser.showOpenDialog(TigerJythonApplication.mainStage)
    if (selectedFile != null)
      getEmptyTextFile match {
        case Some(tab) =>
          tab.loadFile(selectedFile)
        case None =>
          val tab = editor.PythonEditorTab()
          tab.loadFile(selectedFile)
          application.tabManager.addTab(tab)
      }
  }

  def save(): Unit =
    application.tabManager.currentFrame match {
      case Some(editorTab: editor.EditorTab) =>
        val f = editorTab.getFile
        if (f == null)
          saveAsFile()
        else
          editorTab.save()
      case _ =>
    }

  def saveAsFile(): Unit =
    application.tabManager.currentFrame match {
      case Some(editorTab: editor.EditorTab) =>
        val f = editorTab.getFile
        if (f != null)
          fileChooser.setInitialDirectory(f)
        val selectedFile = fileChooser.showSaveDialog(TigerJythonApplication.mainStage)
        if (selectedFile != null)
          editorTab.setFile(selectedFile)
      case _ =>
    }

  def run(): Unit =
    application.tabManager.currentFrame match {
      case Some(editorTab: EditorTab) =>
        editorTab.run()
      case _ =>
    }

  def stop(): Unit =
    application.tabManager.currentFrame match {
      case Some(editorTab: EditorTab) =>
        editorTab.stop()
      case _ =>
    }

  protected def createFileMenu: Menu = {
    val menu = new Menu("File")
    UIString("menu.file") += menu.textProperty()
    menu.getItems.addAll(
      item("new", _ => newFile()),
      item("open", _ => openFile()),
      item("save", _ => save()),
      item("saveas", _ => saveAsFile()),
      new SeparatorMenuItem(),
      item("preferences", _ => application.showPreferences()),
      new SeparatorMenuItem(),
      item("quit", _ => application.handleCloseRequest())
    )
    menu
  }

  protected def createEditMenu: Menu = {
    val menu = new Menu("Edit")
    UIString("menu.edit") += menu.textProperty()
    menu.getItems.addAll(
      item("undo", _ => {}),
      new SeparatorMenuItem(),
      cutItem,
      copyItem,
      pasteItem
    )
    menu
  }

  protected def createRunMenu: Menu = {
    val menu = new Menu("Run")
    UIString("menu.run") += menu.textProperty()
    menu.getItems.addAll(
      item("run", _ => run()),
      item("abort", _ => stop())
    )
    menu
  }

  def createMenu: MenuBar = {
    val menuBar = new MenuBar()
    menuBar.setUseSystemMenuBar(true)
    menuBar.getMenus.addAll(
      createFileMenu,
      createEditMenu,
      createRunMenu
    )
    menuBar
  }
}
