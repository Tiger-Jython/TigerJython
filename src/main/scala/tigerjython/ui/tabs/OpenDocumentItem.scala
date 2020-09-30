/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.tabs

import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, Button, ButtonType}
import javafx.scene.layout.{Priority, VBox}
import javafx.scene.{Group, Node}
import javafx.scene.paint.Color
import javafx.scene.shape.{Line, Rectangle}
import tigerjython.files.Document

/**
 * @author Tobias Kohn
 */
class OpenDocumentItem(val parentFrame: OpenDocumentTab, val document: Document) extends DocumentItem {

  protected val deleteBtn: Node = createDeleteBtn()

  {
    titleLabel.textProperty().set(document.name.get)
    val s = document.pathString.get
    descriptionLabel.textProperty().set("%s\n%d lines, %s".format(
      if (s != null) s else "", document.numberOfLines, document.getDateString
    ))
    setRight(deleteBtn)
  }

  private lazy val lines: Array[(Int, Int)] = {
    val lns = getLines
    var i = lns.length
    while (i > 0 && lns(i-1) == (0, 0))
      i -= 1
    lns.take(i)
  }

  private def getLines: Array[(Int, Int)] = {
    val result = collection.mutable.ArrayBuffer[(Int, Int)]()
    for (line <- document.text.get.split('\n')) {
      var indent = line.segmentLength(_ <= ' ')
      var len = line.length
      while (len > indent && line(len-1) <= ' ')
        len -= 1
      indent = indent / 2
      len = (len / 2) min 22
      if (indent < len)
        result += ((indent, len))
      else
        result += ((0, 0))
      if (result.length > 20)
        return result.toArray
    }
    result.toArray
  }

  protected def createDeleteBtn(): Node = {
    val result = new Button()
    result.setGraphic(createDeleteIcon())
    result.setOnAction(_ => {
      val alert = new Alert(AlertType.CONFIRMATION, "Do you really want to delete '%s'?".format(document.name.get()),
        ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
      alert.showAndWait()
      if (alert.getResult == ButtonType.YES) {
        if (document.isOpen && document.frame != null)
          document.frame.close()
        document.delete()
        parentFrame.update()
      }
    })
    result.getStyleClass.add("delete-btn")

    val filler = new VBox()
    VBox.setVgrow(filler, Priority.ALWAYS)

    val bar = new VBox()
    bar.getChildren.addAll(
      filler,
      result
    )
    bar
  }

  protected def createDeleteIcon(): Node = {
    val result = new Group()
    result.getChildren.addAll(
      new Line(0, 1, 12, 1),
      new Line(3, 0, 9, 0),
      new Line(2, 4, 2, 12),
      new Line(2, 12, 10, 12),
      new Line(10, 12, 10, 4),
    )
    result
  }

  override protected def createIcon(): Node = {
    val result = new Rectangle(42, 59)
    result.setStroke(Color.BLACK)
    result.getStyleClass.add("paper")
    val outline = new Rectangle(60, 59)
    outline.setFill(Color.TRANSPARENT)
    val g = new Group()
    g.getChildren.addAll(
      outline, result
    )
    if (lines.nonEmpty)
      for (i <- lines.indices) {
        val (start, end) = lines(i)
        if (start < end) {
          val l = new Line(10 + start, 10 + 2 * i, 10 + end, 10 + 2 * i)
          l.setStroke(Color.GRAY)
          g.getChildren.add(l)
        }
      }
    else
      for (i <- 0 to 13) {
        val l = new Line(10, 10 + 3 * i, 32, 10 + 3 * i)
        l.setStroke(Color.GRAY)
        g.getChildren.add(l)
      }
    g
  }

  def onClicked(): Unit =
    document.show()

  override def onMouseEnter(): Unit =
    parentFrame.setPreviewText(document.text.get)

  override def onMouseLeave(): Unit =
    parentFrame.setPreviewText("")
}