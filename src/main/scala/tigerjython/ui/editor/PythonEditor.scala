/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.editor

import java.time.Duration
import javafx.concurrent.Task
import javafx.scene.shape.Rectangle
import tigerjython.ui.ZoomMixin
import tigerjython.ui.editing._

/**
 * The Python editor inherits from _RichTextFX_'s `CodeArea` and customises it for highlighting Python code, allowing
 * to zoom in and out of text, or to save the text content after two seconds of no user input.
 *
 * @author Tobias Kohn
 */
class PythonEditor extends PythonCodeArea with ZoomMixin {

  protected val gutterRect = new Rectangle()

  override protected def layoutChildren(): Unit = {
    try {
      val children = getChildren
      if (!(children.get(0) eq gutterRect))
        children.add(0, gutterRect)
      val index = visibleParToAllParIndex(0)
      val wd = getParagraphGraphic(index).prefWidth(-1)
      gutterRect.setWidth(wd)
    } catch {
      case _: Throwable =>
    }
    super.layoutChildren()
  }

  {
    setParagraphGraphicFactory(TJLineNumberFactory.get(this))
    gutterRect.heightProperty.bind(this.heightProperty)
    gutterRect.getStyleClass.add("lineno")

    this.multiPlainChanges()
      .successionEnds(Duration.ofMillis(2500))
      .supplyTask(() => autoSaveAsync())
      .awaitLatest(this.multiPlainChanges())
      .observe(_ => {})
  }

  private def autoSaveAsync(): Task[Unit] =
    BackgroundSaver.execute(onAutoSave)

  var onAutoSave: ()=>Unit = _
}