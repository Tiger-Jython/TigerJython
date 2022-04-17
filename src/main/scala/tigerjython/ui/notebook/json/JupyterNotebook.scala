/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.notebook.json

/**
 * This class represents a Jupyter notebook and is used to import Jupyter notebooks.  TigerJython does not provide
 * full compatibility, but merely support for the basic functionality.  The main idea is to allow teachers to
 * distribute tutorials etc. as notebooks for students to then work through.
 */
class JupyterNotebook(source: JSONValue) {

  val cells: Array[JupyterCell] = readCells(source)

  private def checkLanguage(source: JSONValue): Boolean =
    source("language_info.name") match {
      case Some(JSONString(language)) =>
        language.toLowerCase == "python"
      case None =>
        true
      case _ =>
        false
    }

  private def readCells(source: JSONValue): Array[JupyterCell] =
    if (checkLanguage(source))
      source("cells") match {
        case Some(JSONArray(cells)) =>
          val result = collection.mutable.ArrayBuffer[JupyterCell]()
          for (cell <- cells)
            cell.asString("cell_type") match {
              case "code" =>
                val outputs = collection.mutable.ArrayBuffer[CellOutput]()
                cell("outputs") match {
                  case Some(JSONArray(output_values)) =>
                    for (outp <- output_values)
                      outp.asString("output_type") match {
                        case "error" =>
                          outputs += ErrorOutput(
                            outp.asString("ename"), outp.asString("evalue"),
                            outp.asString("traceback")
                          )
                        case "execute_result" =>
                          val txt = outp.asString("text/plain")
                          if (txt != null)
                            outputs += TextOutput(txt)
                          val pic = outp.asString("image/png")
                          if (pic != null) {
                            // TODO: include picture... (-> attachements)
                          }
                        case "stream" =>
                          outputs += TextOutput(outp.asString("text"))
                        case _ =>
                      }
                  case _ =>
                }
                result += CodeCell(cell.asString("source"), outputs.toArray)
              case "markdown" =>
                result += MarkdownCell(cell.asString("source"))
              case "raw" =>
                result += RawTextCell(cell.asString("source"))
              case _  =>
            }
          result.toArray
        case _ =>
          Array()
      }
    else
      Array()
}
object JupyterNotebook {

  def loadFromFile(filename: java.io.File): JupyterNotebook = {
    val bufferedSource = scala.io.Source.fromFile(filename)
    try {
      loadFromString(bufferedSource.getLines.mkString("\n"))
    } finally {
      bufferedSource.close()
    }
  }

  def loadFromFile(filename: String): JupyterNotebook = {
    val bufferedSource = scala.io.Source.fromFile(filename)
    try {
      loadFromString(bufferedSource.getLines.mkString("\n"))
    } finally {
      bufferedSource.close()
    }
  }

  def loadFromString(data: String): JupyterNotebook =
    try {
      val reader = new JSONReader(data)
      val json = reader.parse()
      new JupyterNotebook(json)
    } catch {
      case _: Throwable =>
        null
    }
}
