/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui

import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.Parent
import javafx.scene.layout.{Pane, Region}

/**
 * JavaFX provides a nice feature to scale the contents of a control.  Unfortunately, it does not adapt to the size
 * of the surrounding window, so that we end up seeing only part of the overall control, or having large margins around
 * its contents.  This Pane here adjusts the width and height as reported to its children in line with the scaling,
 * rectifying the problem and making sure that the entire contents are displayed correctly.
 *
 * See, e.g.: http://johnthecodingarchitect.blogspot.com/2013/11/scaling-vs-zooming-in-javafx.html
 *
 * @author Tobias Kohn
 */
class ZoomingPane extends Pane {

  private val zoomFactor: DoubleProperty = new SimpleDoubleProperty(1.0)

  {
    zoomFactor.addListener(new ChangeListener[Number] {
      override def changed(observableValue: ObservableValue[_ <: Number], t: Number, t1: Number): Unit = {
        val factor = t1.doubleValue()
        setScaleX(factor)
        setScaleY(factor)
        requestLayout()
      }
    })
  }

  parentProperty().addListener(new ChangeListener[Parent] {
    override def changed(observableValue: ObservableValue[_ <: Parent], oldParent: Parent, newParent: Parent): Unit =
      newParent match {
        case region: Region =>
          prefWidthProperty().bind(region.widthProperty())
          prefHeightProperty().bind(region.heightProperty())
        case _ =>
      }
  })

  def getZoomFactor: Double = zoomFactor.get()

  def setZoomFactor(factor: Double): Unit = zoomFactor.set(factor)

  def zoomFactorProperty: DoubleProperty = zoomFactor

  override protected def layoutChildren(): Unit = {
    import javafx.geometry.Pos
    val pos = Pos.CENTER
    val width = getWidth
    val height = getHeight
    val top = getInsets.getTop
    val right = getInsets.getRight
    val left = getInsets.getLeft
    val bottom = getInsets.getBottom
    val wd = width - left - right
    val ht = height - top - bottom
    val contentWidth = (width - left - right) / zoomFactor.get
    val contentHeight = (height - top - bottom) / zoomFactor.get
    val l = (wd - contentWidth) / 2
    val t = (ht - contentHeight) / 2
    getChildren.forEach(child => {
      layoutInArea(child, left + l, top + t, contentWidth, contentHeight, 0, null, pos.getHpos, pos.getVpos)
    })
  }
}
