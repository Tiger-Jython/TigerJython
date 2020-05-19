/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.editor

import java.util.function.IntFunction

import javafx.beans.property.{DoubleProperty, SimpleDoubleProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.paint.Color
import org.fxmisc.richtext.GenericStyledArea
import org.reactfx.collection.LiveList
import org.reactfx.value.Val

/**
 * Most of this code is basically just a Scala version of the `LineNumberFactory` included in _RichTextFX_.  See:
 * org.fxmisc.richtext.LineNumberFactory.
 *
 * Besides being able to follow the width of line-number labels, we have also removed default font etc. so that it can
 * be styled through CSS.  In particular, when changing the font size of the editor, the line numbers will also pick
 * up the new font size.
 *
 * @author Tomas Mikula
 * @author Tobias Kohn
 */
object TJLineNumberFactory {

  private val DEFAULT_INSETS = new Insets(0.0, 5.0, 0.0, 5.0)

  private val DEFAULT_BACKGROUND = new Background(
    new BackgroundFill(Color.GOLD, null, null)
  )

  def get(area: GenericStyledArea[_, _, _]): IntFunction[Node] =
    get(area, (digits: Int) => "%1$" + digits + "s")

  def get(area: GenericStyledArea[_, _, _], format: IntFunction[String]) =
    new TJLineNumberFactory(area, format)

  val widthProperty: DoubleProperty = new SimpleDoubleProperty(1.0)

  private var currentLineNo: Label = _

  private val widthListener = new ChangeListener[Number] {
    override def changed(observableValue: ObservableValue[_ <: Number], t: Number, t1: Number): Unit = {
      widthProperty.setValue(t1)
    }
  }
}
class TJLineNumberFactory private (val area: GenericStyledArea[_, _, _],
                                   val format: IntFunction[String]) extends IntFunction[Node] {

  import TJLineNumberFactory._

  final private val nParagraphs: Val[Integer] = LiveList.sizeOf(area.getParagraphs)

  override def apply(idx: Int): Node = {
    val formatted = nParagraphs.map((n: Integer) => format(idx + 1, n))
    val lineNo = new Label
    lineNo.setBackground(DEFAULT_BACKGROUND)
    lineNo.setPadding(DEFAULT_INSETS)
    lineNo.setAlignment(Pos.TOP_RIGHT)
    lineNo.getStyleClass.add("lineno")
    lineNo.textProperty.bind(formatted.conditionOnShowing(lineNo))
    lineNo.prefWidthProperty.bind(lineNo.heightProperty.multiply(2.25))
    if (currentLineNo != null)
      currentLineNo.widthProperty().removeListener(widthListener)
    currentLineNo = lineNo
    lineNo.widthProperty.addListener(widthListener)
    lineNo
  }

  private def format(x: Int, max: Int): String = {
    val digits = Math.floor(Math.log10(max)).toInt + 1
    String.format(format.apply(digits), x)
  }
}
