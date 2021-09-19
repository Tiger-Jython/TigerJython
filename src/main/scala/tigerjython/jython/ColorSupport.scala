/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.jython

import org.python.core._

/**
 * This class provides support for colours in TigerJython, in particular the `makeColor` function.
 *
 * @author Tobias Kohn
 */
object ColorSupport {

  private val scaling: Double = 255

  def colorFromCMY(c: Double, m: Double, y: Double): java.awt.Color = {
    null
  }

  def colorFromCMYK(c: Double, m: Double, y: Double, k: Double): java.awt.Color = {
    null
  }

  def colorFromHSB(h: Double, s: Double, b: Double): java.awt.Color = {
    val hue = ((h max 0) min 1.0).toFloat
    val sat = ((s max 0) min 1.0).toFloat
    val bri = ((b max 0) min 1.0).toFloat
    java.awt.Color.getHSBColor(hue, sat, bri)
  }

  def colorFromRGB(r: Double, g: Double, b: Double): java.awt.Color = {
    val red = ((r max 0) * scaling).toInt min 255
    val green = ((g max 0) * scaling).toInt min 255
    val blue = ((b max 0) * scaling).toInt min 255
    new java.awt.Color(red, green, blue)
  }

  def colorFromString(value: String): java.awt.Color = {
    val color = javafx.scene.paint.Color.valueOf(value)
    val r = (color.getRed * 255).toInt
    val g = (color.getGreen * 255).toInt
    val b = (color.getBlue * 255).toInt
    new java.awt.Color(r, g, b)
  }

  def makeColor(arg: PyObject): PyColor =
    arg match {
      case color: PyColor =>
        color
      case s: PyString =>
        new PyColor(colorFromString(s.asString))
      case i: PyInteger =>
        val value = i.asInt() & 0x0FFFFFF
        new PyColor(new java.awt.Color(value))
      case t: PyTuple if t.__len__() == 2 =>
        makeColor(t.pyget(0), t.pyget(1))
      case t: PyTuple if t.__len__() >= 3 =>
        null
      case _ =>
        null
    }

  def makeColor(arg1: PyObject, arg2: PyObject): PyColor =
    (arg1, arg2) match {
      case (color: PyColor, alpha: PyFloat) =>
        val c = new PyColor(color)
        c.setAlpha(alpha.asDouble().toFloat)
        c
      case (color: PyString, alpha: PyFloat) =>
        makeColor(makeColor(color), alpha)
      case (color1: PyColor, color2: PyColor) =>
        mixColors(color1, color2)
      case (color1: PyString, color2: PyString) =>
        makeColor(makeColor(color1), makeColor(color2))
      case (color1: PyString, color2: PyColor) =>
        makeColor(color1, makeColor(color2))
      case (color1: PyColor, color2: PyString) =>
        makeColor(color1, makeColor(color2))
      case (colormode: PyString, value: PyInteger) =>
        createColor(colormode.asString, value)
      case (colormode: PyString, value: PyFloat) =>
        createColor(colormode.asString, value)
      case (colormode: PyString, value: PyTuple) =>
        val len = value.__len__()
        val args = collection.mutable.ArrayBuffer[PyObject]()
        for (i <- 0 until len)
          value.pyget(i) match {
            case f: PyFloat =>
              args += f
            case i: PyInteger =>
              args += i
            case _ =>
              // TODO: raise error
          }
        createColor(colormode.asString, args.toSeq: _*)
      case _ =>
        null
    }

  def makeColor(arg1: PyObject, arg2: PyObject, arg3: PyObject): PyColor =
    (arg1, arg2, arg3) match {
      case (_: PyFloat | PyInteger, _: PyFloat | PyInteger, _: PyFloat | PyInteger) =>
        createColor("rgb", arg1, arg2, arg3)
      case _ =>
        null
    }

  def makeColor(arg1: PyObject, arg2: PyObject, arg3: PyObject, arg4: PyObject): PyColor =
    (arg1, arg2, arg3, arg4) match {
      case (_: PyFloat | PyInteger, _: PyFloat | PyInteger, _: PyFloat | PyInteger, _: PyFloat | PyInteger) =>
        createColor("rgba", arg1, arg2, arg3, arg4)
      case _ =>
        null
    }

  protected def createColor(colormode: String, values: PyObject*): PyColor =
    colormode.toLowerCase match {
      case "cmy" =>
        null
      case "cmyk" =>
        if (values.length == 4)
          new PyColor(colorFromCMYK(values(0).asDouble(), values(1).asDouble,
            values(2).asDouble(), values(3).asDouble))
        else
          null
      case "hsb" | "hsv" =>
        if (values.length == 3)
          new PyColor(colorFromHSB(values(0).asDouble(), values(1).asDouble, values(2).asDouble()))
        else
          null
      case "hsl" =>
        null
      case "rgb" =>
        if (values.length == 1 && values(0).isInstanceOf[PyInteger])
          new PyColor()
        else if (values.length == 3)
          new PyColor(colorFromRGB(values(0).asDouble(), values(1).asDouble, values(2).asDouble()))
        else
          null
      case "rgba" =>
        null
      case "rainbow" =>
        null
      case "vga" =>
        null
      case _ =>
        null
    }

  protected def mixColors(color1: PyColor, color2: PyColor): PyColor = {
    val red = (color1.getRed + color2.getRed) / 2
    val green = (color1.getGreen + color2.getGreen) / 2
    val blue = (color1.getBlue + color2.getBlue) / 2
    new PyColor(colorFromRGB(red, green, blue))
  }
}
