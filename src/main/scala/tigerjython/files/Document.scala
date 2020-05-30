/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.files

import java.io.{FileWriter, PrintWriter}
import java.nio.file.{Path, Paths}
import java.text.DateFormat
import java.util.Date
import java.util.prefs.{Preferences => JPreferences}

import javafx.beans.property._
import tigerjython.execute.PythonCodeTranslator
import tigerjython.ui.{TabFrame, TigerJythonApplication}
import tigerjython.utils._

/**
 * In order to store (meta-)properties about the document being edited (such as the position of the caret, the modules
 * used, etc.), we use this `Document` class.  It allows us to restore a complete edit session later on.
 *
 * @author Tobias Kohn
 */
class Document(protected val prefNode: JPreferences) {

  private var _tempFile: java.io.File = _

  var frame: TabFrame = _

  // We ensure that the preferences contain some basic fields such as the date of its creation and the date when it
  // has been modified last
  {
    val now = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date())
    if (prefNode.get("created", null) == null)
      prefNode.put("created", now)
    if (prefNode.get("last-modified", null) == null)
      prefNode.put("last-modified", now)
  }

  val caretPosition: IntegerProperty = new PrefIntegerProperty(prefNode, "caret-pos", 0)

  def close(): Unit = {
    prefNode.putBoolean("open", false)
    caretPosition.unbind()
    frame = null
  }

  val description: StringProperty = new PrefStringProperty(prefNode, "description", "")

  def exists: Boolean =
    path match {
      case Some(p) =>
        p.toFile.exists()
      case _ =>
        false
    }

  def file: java.io.File =
    path match {
      case Some(p) =>
        p.toFile
      case _ =>
        null
    }

  def getDefaultFileSuffix: String = ".py"

  /**
   * Creates a temporary file for execution, even if the document does otherwise not have an actual file backing it
   * up.
   */
  def getExecutableFile: java.io.File = {
    var result = file
    if (result == null) {
      if (_tempFile == null) {
        _tempFile = java.io.File.createTempFile(name.getValue + " (", ")" + getDefaultFileSuffix)
        _tempFile.deleteOnExit()
      }
      result = _tempFile
    }
    val text =
      PythonCodeTranslator.translate(this.text.get) match {
        case Some(text) =>
          text
        case None =>
          this.text.get
      }
    synchronized {
      val writer = new FileWriter(result)
      val printer = new PrintWriter(writer)
      printer.print(text)
      printer.close()
    }
    result
  }

  val imports: StringProperty = new PrefStringProperty(prefNode, "imports", "")

  def index: Int = prefNode.getInt("index", 0)

  def isOpen: Boolean = prefNode.getBoolean("open", false)

  def isPath(p: Path): Boolean =
    path match {
      case Some(myPath) =>
        myPath.compareTo(p) == 0
      case None =>
        false
    }

  /**
   * Returns the date the document has last been modified.
   */
  def lastModified: Date =
    DateFormat.getDateInstance(DateFormat.MEDIUM).parse(prefNode.get("last-modified", null))

  /**
   * Returns the date the document has last been modified.
   *
   * This method returns the string
   */
  def lastModifiedString: String =
    prefNode.get("last-modified", "?")

  def load(): String = synchronized {
    val f = file
    if (f != null && f.exists()) {
      val source = scala.io.Source.fromFile(file)
      val result = source.getLines.mkString("\n")
      this.text.setValue(result)
      result
    } else
      this.text.get()
  }

  /**
   * Call `modified` to mark the document as having been modified today.
   */
  def modified(): Unit =
    prefNode.put("last-modified", DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date()))

  val name: StringProperty = new PrefStringProperty(prefNode, "name")

  def open(f: TabFrame): Unit = {
    prefNode.putBoolean("open", true)
    frame = f
  }

  def path: Option[Path] = {
    val s = pathString.get
    if (s != null && s != "")
      Some(Paths.get(s))
    else
      None
  }

  val pathString: ReadOnlyStringProperty = new PrefStringProperty(prefNode, "path")

  def save(text: String, caretPos: Int): Unit = synchronized {
    val f = file
    if (f != null) {
      val writer = new FileWriter(file)
      val printer = new PrintWriter(writer)
      printer.print(text)
      printer.close()
    }
    caretPosition.setValue(caretPos)
    this.text.setValue(text)
    modified()
    setDescriptionFromText(text)
  }

  private def setDescriptionFromText(text: String): Unit =
    description.setValue(text.split('\n').take(3).mkString("\n"))

  def setPath(path: Path): Unit = {
    pathString.asInstanceOf[PrefStringProperty].setValue(path.toAbsolutePath.toString)
    val n = path.getFileName.toString
    if (n.toLowerCase.endsWith(".py"))
      name.setValue(n.dropRight(3))
    else
      name.setValue(n)
  }

  def show(): Unit =
    TigerJythonApplication.tabManager.openDocument(this)

  val text: StringProperty = new PrefStringProperty(prefNode, "text")
}
