/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui

import javafx.beans.property.{DoubleProperty, SimpleDoubleProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.Event
import javafx.geometry.Bounds
import javafx.scene.Node
import javafx.scene.input.{ScrollEvent, ZoomEvent}
import tigerjython.core.Preferences

/**
 * For various text-based controls, it is convenient being able to zoom in and out using `CTRL` and the mouse wheel
 * (as in internet browsers) or standard gestures.  This mixin provides exactly this functionality by setting the
 * font size.
 *
 * @author Tobias Kohn
 */
trait ZoomMixin { self: Node =>

  // These values are taken from Mozilla Firefox and make for a nice user experience
  private val zoomFactors = Array[Double](
    1.0/4, 1.0/3, 1.0/2, 2.0/3,
    0.8, 0.9, 1.0, 1.1, 1.2,
    4.0/3, 3.0/2, 5.0/3,
    2.0, 2.4, 3.0, 5.0
  )

  private var zoomIndex = zoomFactors.indexOf(1.0)

  private def setZoomIndex(index: Int): Unit = {
    zoomIndex = index
    val size: Double = Preferences.fontSize.get * zoomFactors(index)
    setStyle("-fx-font-size: %g; -fx-font-family: \"%s\";".format(
      size,
      Preferences.fontFamily.get
    ))
  }

  Preferences.fontFamily.addListener(new ChangeListener[String] {
    override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
      setZoomIndex(zoomIndex)
    }
  })
  Preferences.fontSize.addListener(new ChangeListener[Number] {
    override def changed(observableValue: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit = {
      setZoomIndex(zoomIndex)
    }
  })

  val zoomProperty: DoubleProperty = new SimpleDoubleProperty(1.0) with ObservableValue[Number] {

    override def set(v: Double): Unit = {
      val index = findClosestIndexForZoomFactor(math.abs(v))
      setZoomIndex(index)
      super.set(zoomFactors(index))
    }
  }

  def getScaledFontSize: Double = Preferences.fontSize.get * zoomFactors(zoomIndex)

  def getZoom: Double = zoomProperty.getValue

  def setZoom(factor: Double): Unit =
    zoomProperty.setValue(factor)

  private def findClosestIndexForZoomFactor(factor: Double): Int =
    if (factor <= zoomFactors.head)
      0
    else if (factor >= zoomFactors.last)
      zoomFactors.length - 1
    else {
      val distances = zoomFactors.map(x => math.abs(x - factor))
      var index = 0
      var min = zoomFactors.last
      for (i <- distances.indices;
           d = distances(i))
        if (d < min) {
          min = d
          index = i
        }
      index
    }

  private def getCenter(bounds: Bounds): (Double, Double) =
    try {
      (bounds.getCenterX, bounds.getCenterY)
    } catch {
      case _: NoSuchMethodError =>
        (bounds.getMinX + bounds.getMaxX / 2, (bounds.getMinY + bounds.getMaxY) / 2)
    }

  self.addEventFilter(ScrollEvent.ANY, (event: ScrollEvent) => {
    if (event.isControlDown) {
      val y = event.getDeltaY
      val t = (y / event.getMultiplierY).toInt
      if (t != 0) {
        val currentIndex = zoomIndex
        val index = ((currentIndex + t) max 0) min (zoomFactors.length - 1)
        if (index != currentIndex) {
          val factor: Double = zoomFactors(index) / zoomFactors(zoomIndex)
          val localBounds = self.getBoundsInLocal
          val screenBounds = self.localToScreen(localBounds)
          val (localX, localY) = getCenter(localBounds)
          val (screenX, screenY) = getCenter(screenBounds)
          val zoomEvent = new ZoomEvent(ZoomEvent.ZOOM,
            localX, localY,
            screenX, screenY,
            event.isShiftDown, event.isControlDown,
            event.isAltDown, event.isMetaDown,
            false, false,
            factor, zoomFactors(index), null
          )
          Event.fireEvent(self, zoomEvent)
        }
      }
      event.consume()
    }
  })

  self.addEventFilter(ZoomEvent.ANY, (event: ZoomEvent) => {
    val f = event.getZoomFactor
    if (f != 0)
      setZoom(getZoom * f)
  })
}
