package tigerjython.files

import java.io.File
import java.nio.file.Path
import java.util.prefs.{Preferences => JPreferences}

import sun.security.action.GetPropertyAction
import tigerjython.execute.ExecLanguage
import tigerjython.ui.{TigerJythonApplication, editor}

/**
 * This is the companion object to `Document`s.  In maintains a list of documents ever edited, where to find them as
 * well as the position of the caret within it, etc.
 *
 * @author Tobias Kohn
 */
object Documents {

  protected lazy val preferences: JPreferences = JPreferences.userNodeForPackage(getClass)

  private val documents = collection.mutable.ArrayBuffer[Document]()

  private var currentIndex: Int = 0

  def apply(path: Path): Document =
    Documents.getDocumentForPath(path)

  def apply(path: java.io.File): Document =
    Documents.getDocumentForFile(path)

  def createDocument(): Document = {
    val node = preferences.node(createDocumentName)
    node.putInt("index", currentIndex)
    val result = new Document(node)
    documents += result
    result
  }

  private def createDocumentName: String = {
    currentIndex += 1
    val name = currentIndex.toHexString.toLowerCase
    preferences.putInt("index", currentIndex)
    val prefix =
      if (name.length < 4)
        4 - name.length
      else
        name.length % 2
    "file_x%s%s".format("0" * prefix, name)
  }

  def findDocumentWithName(name: String): Option[Document] = {
    for (doc <- documents)
      if (doc.name.get == name)
        return Some(doc)
    None
  }

  def getDocumentForFile(file: java.io.File): Document =
    if (file != null)
      getDocumentForPath(file.toPath)
    else
      createDocument()

  def getDocumentForPath(path: Path): Document =
    if (path != null) {
      for (doc <- documents)
        if (doc.isPath(path))
          return doc
      val result = createDocument()
      result.setPath(path)
      result
    } else
      createDocument()

  def getListOfDocuments: Array[Document] = {
    documents.toArray
  }

  def getListOfDocumentNames: Set[String] = {
    val result = collection.mutable.Set[String]()
    for (doc <- documents)
      result.add(doc.name.get)
    result.toSet
  }

  def importDocument(file: java.io.File): Document = {
    val result = createDocument()
    result.importFromFile(file)
    result
  }

  lazy val tempDir: File =
    new File(GetPropertyAction.privilegedGetProperty("java.io.tmpdir"))

  private[files]
  def removeDocument(document: Document): Unit = {
    val idx = documents.indexOf(document)
    if (document != null && idx >= 0)
      documents.remove(idx)
  }

  protected[files]
  def makeNameUnique(name: String, index: Int = 0): String = {
    val n =
      if (index > 0)
        "%s (%d)".format(name, index)
      else
        name
    for (document <- documents)
      if (document.name.get == n) {
        return makeNameUnique(name, index+1)
      }
    n
  }

  def saveAllExecutables(execLanguage: ExecLanguage.Value): Unit = {
    for (document <- documents)
      document.saveExecutableFile(execLanguage)
  }

  /**
   * Reads all the documents from the preferences.
   */
  def initialize(): Unit = {
    currentIndex = preferences.getInt("index", currentIndex)
    val openDocuments = collection.mutable.ArrayBuffer[Document]()
    for (childName <- preferences.childrenNames()) {
      val doc = new Document(preferences.node(childName))
      documents += doc
      if (doc.isOpen)
        openDocuments += doc
    }
    for (doc <- openDocuments) {
      val tab = editor.PythonEditorTab(doc)
      TigerJythonApplication.tabManager.addTab(tab)
    }
    if (openDocuments.isEmpty)
      TigerJythonApplication.tabManager.addTab(editor.PythonEditorTab())
  }
}
