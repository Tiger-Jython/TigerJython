package tigerjython.files

import java.nio.file.Path
import java.util.prefs.{Preferences => JPreferences}
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
    Documents.getDocumentForPath(path.toPath)

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

  /**
   * Reads all the documents from the preferences.
   */
  def initialize(): Unit = {
    currentIndex = preferences.getInt("index", currentIndex)
    val openDocuments = collection.mutable.ArrayBuffer[Document]()
    for (childName <- preferences.childrenNames()) {
      val doc = new Document(preferences.node(childName))
      if (doc.exists) {
        documents += doc
        if (doc.isOpen)
          openDocuments += doc
      }
    }
    for (doc <- openDocuments) {
      val tab = editor.PythonEditorTab(doc)
      TigerJythonApplication.tabManager.addTab(tab)
    }
    if (openDocuments.isEmpty)
      TigerJythonApplication.tabManager.addTab(editor.PythonEditorTab())
  }
}
