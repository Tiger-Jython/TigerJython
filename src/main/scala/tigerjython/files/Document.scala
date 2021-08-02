/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.files

import java.io.{File, FileWriter, PrintWriter}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{Files, Path, Paths}
import java.text.{DateFormat, SimpleDateFormat}
import java.time.ZoneId
import java.util.Date
import java.util.prefs.{Preferences => JPreferences}

import javafx.beans.property._
import tigerjython.execute.{ExecLanguage, PythonCodeTranslator}
import tigerjython.ui.{TabFrame, TigerJythonApplication}
import tigerjython.utils._

/**
 * In order to store (meta-)properties about the document being edited (such as the position of the caret, the modules
 * used, etc.), we use this `Document` class.  It allows us to restore a complete edit session later on.
 *
 * @author Tobias Kohn
 */
class Document(protected val prefNode: JPreferences) {

  private val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

  private var _tempFile: java.io.File = _

  var frame: TabFrame = _

  // We ensure that the preferences contain some basic fields such as the date of its creation and the date when it
  // has been modified last
  {
    val now = dateFormat.format(new Date())
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

  def delete(): Unit = {
    prefNode.clear()
    prefNode.removeNode()
    Documents.removeDocument(this)
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

  protected def formatDate(date: Date): String =
    new SimpleDateFormat("d MMM yyyy").format(date)

  def getCreationDate: Date =
    dateFormat.parse(prefNode.get("created", null))

  /**
   * Returns the dates of creation and last modification as a string that is as short as possible, i.e. either
   * `3-5 Mar 2020`, `3 Mar-9 Apr 2020`, or `3 Mar 2020-9 Apr 2021`, respectively.
   */
  def getDateString: String = {
    val d1 = getCreationDate.toInstant.atZone(ZoneId.systemDefault())
    val d2 = lastModified.toInstant.atZone(ZoneId.systemDefault())
    if (d1.getYear == d2.getYear) {
      if (d1.getMonthValue != d2.getMonthValue)
        "%s–%s".format(
          new SimpleDateFormat("d MMM").format(getCreationDate),
          formatDate(lastModified)
        )
      else if (d1.getDayOfMonth != d2.getDayOfMonth)
        "%d–%s".format(d1.getDayOfMonth, formatDate(lastModified))
      else
        formatDate(lastModified)
    } else
      "%s–%s".format(formatDate(getCreationDate), formatDate(lastModified))
  }

  def getDefaultFileSuffix: String = ".py"

  private def createTempFile(): java.io.File =
    new File(Documents.tempDir, name.getValue
      .replace('?', '_').replace('*', '_').replace('&', '_')
      .replace('/', '_').replace('\\', '_').replace('\"', '_') +
      getDefaultFileSuffix)
  /*java.io.File.createTempFile(name.getValue
    .replace('?', '_').replace('*', '_').replace('&', '_')
    .replace('/', '_').replace('\\', '_').replace('\"', '_') +
    " (", ")" + getDefaultFileSuffix)*/

  /**
   * Creates a temporary file for execution, even if the document does otherwise not have an actual file backing it
   * up.
   */
  def getExecutableFile(execLanguage: ExecLanguage.Value): java.io.File = {
    var result = file
    if (result == null) {
      if (_tempFile == null) {
        _tempFile = createTempFile()
        _tempFile.deleteOnExit()
      }
      result = _tempFile
    }
    val text =
      PythonCodeTranslator.translate(this.text.get, execLanguage) match {
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

  def getExecutableFileAsString(execLanguage: ExecLanguage.Value): String =
    PythonCodeTranslator.translate(this.text.get, execLanguage) match {
      case Some(text) =>
        text
      case None =>
        this.text.get
    }

  def saveExecutableFile(execLanguage: ExecLanguage.Value): Unit = {
    getExecutableFile(execLanguage)
  }

  def getSearchScore(filter: SearchFilter): Int =
    filter.getScore(name.get) + filter.getScore(description.get) +
      filter.getScore(text.get) + filter.getScore(pathString.get())

  val imports: StringProperty = new PrefStringProperty(prefNode, "imports", "")

  def importFromFile(file: java.io.File): Unit = synchronized {
    if (file != null && file.exists()) {
      val source = scala.io.Source.fromFile(file)
      val txt = source.getLines().mkString("\n")
      this.text.setValue(txt)
      prefNode.put("source", file.getAbsolutePath)
      val attr = Files.readAttributes(file.toPath, classOf[BasicFileAttributes])
      val createDate = Date.from(attr.creationTime().toInstant)
      if (createDate.before(getCreationDate))
        prefNode.put("created", dateFormat.format(createDate))
      var n = file.getName
      if (n.toLowerCase.endsWith(".py"))
        n = n.dropRight(3)
      n = Documents.makeNameUnique(n)
      name.setValue(n)
    }
  }

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
    dateFormat.parse(prefNode.get("last-modified", null))

  /**
   * Returns the date the document has last been modified.
   *
   * This method returns the string
   */
  def lastModifiedString: String =
    prefNode.get("last-modified", "?")

  def load(): String = synchronized {
    path match {
      case Some(p) =>
        val f = p.toFile
        if (f.exists()) {
          val source = scala.io.Source.fromFile(file)
          val result = source.getLines.mkString("\n")
          this.text.setValue(result)
          val attr = Files.readAttributes(p, classOf[BasicFileAttributes])
          val createDate = Date.from(attr.creationTime().toInstant)
          if (createDate.before(getCreationDate))
            prefNode.put("created", dateFormat.format(createDate))
          result
        } else
          this.text.get()
      case _ =>
        this.text.get
    }
  }

  def loadUndo(): (Array[Byte], String) = synchronized {
    (prefNode.getByteArray("undoIndices", Array()), prefNode.get("undoText", ""))
  }

  /**
   * Call `modified` to mark the document as having been modified today.
   */
  def modified(): Unit =
    prefNode.put("last-modified", DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date()))

  val name: StringProperty = new PrefStringProperty(prefNode, "name")

  def numberOfLines: Int = {
    val txt = text.get()
    if (txt != null && txt != "")
      txt.count(_ == '\n') + 1
    else
      0
  }

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

  def saveCopyToFile(file: java.io.File): Unit = synchronized {
    val writer = new FileWriter(file)
    val printer = new PrintWriter(writer)
    printer.print(text)
    printer.close()
  }

  def saveUndo(indices: Array[Byte], text: String): Unit = synchronized {
    prefNode.putByteArray("undoIndices", indices)
    prefNode.put("undoText", text)
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

  val text: StringProperty = new PrefTextProperty(prefNode, "text")
}
