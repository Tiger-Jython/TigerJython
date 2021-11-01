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

  /**
   * A list with all W3C/HTML color names, including also some of the
   * X11 standard.
   *
   * The color names are all lowercase and spaces have been removed. Where clashes
   * between W3C and X11 occur, the X11-variant has a "x11"-prefix.
   *
   * Source: http://en.wikipedia.org/wiki/X11_color_names
   */
  final val _colorNames = Map(
    "aliceblue" -> 		0xF0F8FF,
    "antiquewhite" -> 	0xFAEBD7,
    "aqua" -> 			0x00FFFF,
    "aquamarine" -> 		0x7FFFD4,
    "azure" -> 			0xF0FFFF,
    "beige" -> 			0xF5F5DC,
    "bisque" -> 			0xFFE4C4,
    "black" -> 			0x000000,
    "blanchedalmond" -> 	0xFFEBCD,
    "blue" -> 			0x0000FF,
    "blueviolet" -> 		0x8A2BE2,
    "brown" -> 			0xA52A2A,
    "burlywood" -> 		0xDEB887,
    "cadetblue" -> 		0x5F9EA0,
    "chartreuse" -> 		0x7FFF00,
    "chocolate" -> 		0xD2691E,
    "coral" -> 			0xFF7F50,
    "cornflower" -> 		0x6495ED,
    "cornsilk" -> 		0xFFF8DC,
    "crimson" -> 			0xDC143C,
    "cyan" -> 			0x00FFFF,
    "darkblue" -> 		0x00008B,
    "darkcyan" -> 		0x008B8B,
    "darkgoldenrod" -> 	0xB8860B,
    "darkgray" -> 		0xA9A9A9,
    "darkgreen" -> 		0x006400,
    "darkkhaki" -> 		0xBDB76B,
    "darkmagenta" -> 		0x8B008B,
    "darkolivegreen" -> 	0x556B2F,
    "darkorange" -> 		0xFF8C00,
    "darkorchid" -> 		0x9932CC,
    "darkred" -> 			0x8B0000,
    "darksalmon" -> 		0xE9967A,
    "darkseagreen" -> 	0x8FBC8F,
    "darkslateblue" -> 	0x483D8B,
    "darkslategray" -> 	0x2F4F4F,
    "darkturqoise" -> 	0x00CED1,
    "darkviolet" -> 		0x9400D3,
    "deeppink" -> 		0xFF1493,
    "deepskyblue" -> 		0x00BFFF,
    "dimgray" -> 			0x696969,
    "dodgerblue" -> 		0x1E90FF,
    "firebrick" -> 		0xB22222,
    "floralwhite" -> 		0xFFFAF0,
    "forestgreen" -> 		0x228B22,
    "fuchsia" -> 			0xFF00FF,
    "gainsboro" -> 		0xDCDCDC,
    "ghostwhite" -> 		0xF8F8FF,
    "gold" -> 			0xFFD700,
    "goldenrod" -> 		0xDAA520,
    "gray" -> 			0x808080,
    "green" -> 			0x008000,
    "greenyellow" -> 		0xADFF2F,
    "honeydew" -> 		0xF0FFF0,
    "hotpink" -> 			0xFF69B4,
    "indianred" -> 		0xCD5C5C,
    "indigo" -> 			0x4B0082,
    "ivory" -> 			0xFFFFF0,
    "khaki" -> 			0xF0E68C,
    "lavender" -> 		0xE6E6FA,
    "lavenderblush" -> 	0xFFF0F5,
    "lawngreen" -> 		0x7CFC00,
    "lemonchiffon" -> 	0xFFFACD,
    "lightblue" -> 		0xADD8E6,
    "lightcoral" -> 		0xF08080,
    "lightcyan" -> 		0xE0FFFF,
    "lightgoldenrod" -> 	0xFAFAD2,
    "lightgray" -> 		0xD3D3D3,
    "lightgreen" -> 		0x90EE90,
    "lightpink" -> 		0xFFB6C1,
    "lightsalmon" ->		0xFFA07A,
    "lightseagreen" -> 	0x20B2AA,
    "lightskyblue" -> 	0x87CEFA,
    "lightslategray" -> 	0x778899,
    "lightsteelblue" -> 	0xB0C4DE,
    "lightyellow" -> 		0xFFFFE0,
    "lime" -> 			0x00FF00,
    "limegreen" -> 		0x32CD32,
    "linen" -> 			0xFAF0E6,
    "magenta" -> 			0xFF00FF,
    "maroon" -> 			0x7F0000,
    "mediumaquamarine" -> 0x66CDAA,
    "mediumblue" -> 		0x0000CD,
    "mediumorchid" -> 	0xBA55D3,
    "mediumpurple" -> 	0x9370DB,
    "mediumseagreen" -> 	0x3CB371,
    "mediumslateblue" -> 	0x7B68EE,
    "mediumspringgreen"-> 0x00FA9A,
    "mediumturqoise" -> 	0x48D1CC,
    "mediumvioletred" -> 	0xC71585,
    "midnightblue" -> 	0x191970,
    "mintcream" -> 		0xF5FFFA,
    "mistyrose" -> 		0xFFE4E1,
    "moccasin" -> 		0xFFE4B5,
    "navajowhite" -> 		0xFFDEAD,
    "navy" -> 			0x000080,
    "navyblue" ->   0x000080,
    "oldlace" -> 			0xFDF5E6,
    "olive" -> 			0x808000,
    "olivedrab" -> 		0x6B8E23,
    "orange" -> 			0xFFA500,
    "orangered" -> 		0xFF4500,
    "orchid" -> 			0xDA70D6,
    "palegoldenrod" -> 	0xEEE8AA,
    "palegreen" -> 		0x98FB98,
    "paleturquoise" -> 	0xAFEEEE,
    "palevioletred" -> 	0xDB7093,
    "papayawhip" -> 		0xDB7093,
    "peachpuff" -> 		0xFFDAB9,
    "peru" -> 			0xCD853F,
    "pink" ->				0xFFC0CB,
    "plum" -> 			0xDDA0DD,
    "powderblue" -> 		0xB0E0E6,
    "purple" -> 			0x7F007F,
    "red" -> 				0xFF0000,
    "rosybrown" -> 		0xBC8F8F,
    "royalblue" -> 		0x4169E1,
    "saddlebrown" -> 		0x8B4513,
    "salmon" -> 			0xFA8072,
    "sandybrown" -> 		0xF4A460,
    "seagreen" -> 		0x2E8B57,
    "seashell" -> 		0xFFF5EE,
    "sienna" -> 			0xA0522D,
    "silver" -> 			0xC0C0C0,
    "skyblue" -> 			0x87CEEB,
    "slateblue" -> 		0x6A5ACD,
    "slategray" -> 		0x708090,
    "snow" -> 			0xFFFAFA,
    "springgreen" ->	 	0x00FF7F,
    "stealblue" -> 		0x4682B4,
    "tan" -> 				0xD2B48C,
    "teal" -> 			0x008080,
    "thistle" -> 			0xD8BFD8,
    "tomato" -> 			0xFF6347,
    "turquoise" -> 		0x40E0D0,
    "violet" -> 			0xEE82EE,
    "wheat" -> 			0xF5DEB3,
    "white" -> 			0xFFFFFF,
    "whitesmoke" -> 		0xF5F5F5,
    "yellow" -> 			0xFFFF00,
    "yellowgreen" -> 		0x9ACD32,
    "x11gray" -> 			0xBEBEBE,
    "x11grey" -> 			0xBEBEBE,
    "x11green" ->			0x00FF00,
    "x11maroon" ->		0xB03060,
    "x11purple" ->		0xA020F0,
    "w3cgray" ->			0x808080,
    "w3cgrey" ->			0x808080,
    "w3cgreen" ->			0x008000,
    "w3cmaroon" ->		0x7F0000,
    "w3cpurple" ->		0x7F007F
  );

  /**
   * These is the old VGA-color-palette.
   *
   * Source: http://en.wikipedia.org/wiki/Video_Graphics_Array
   */
  final val _vgaColors = Array(
    // Standard 16 colors
    0x000000, 0x0000AA, 0x00AA00, 0x00AAAA,
    0xAA0000, 0xAA00AA, 0xAAAA00, 0xAAAAAA,
    0X555555, 0x5555FF, 0x55FF55, 0x55FFFF,
    0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF,
    // Gray
    0x000000, 0x101010, 0x202020, 0x353535,
    0x454545, 0x555555, 0x656565, 0x757575,
    0x8A8A8A, 0x9A9A9A, 0xAAAAAA, 0xBABABA,
    0xCACACA, 0xDFDFDF, 0xEFEFEF, 0xFFFFFF,
    // Through the rainbow 1
    0x0000FF, 0x4100FF, 0x8200FF, 0xBE00FF,
    0xFF00FF, 0xFF00BE, 0xFF0082, 0xFF0041,
    0xFF0000, 0xFF4100, 0xFF8200, 0xFFBE00,
    0xFFFF00, 0xBEFF00, 0x82FF00, 0x41FF00,
    0x00FF00, 0x00FF41, 0x00FF82, 0x00FFBE,
    0x00FFFF, 0x00BEFF, 0x0082FF, 0x0041FF,
    // Through the rainbox 2
    0x8282FF, 0x9E82FF, 0xBE82FF, 0xDF82FF,
    0xFF82FF, 0xFF82DF, 0xFF82BE, 0xFF829E,
    0xFF8282, 0xFF9E82, 0xFFBE82, 0xFFDF82,
    0xFFFF82, 0xDFFF82, 0xBEFF82, 0x9EFF82,
    0x82FF82, 0x82FF9E, 0x82FFBE, 0x82FFDF,
    0x82FFFF, 0x82DFFF, 0x82BEFF, 0x829EFF,
    // Through the rainbow 3
    0xBABAFF, 0xCABAFF, 0xDFBAFF, 0xEFBAFF,
    0xFFBAFF, 0xFFBAEF, 0xFFBADF, 0xFFBACA,
    0xFFBABA, 0xFFCABA, 0xFFDFBA, 0xFFEFBA,
    0xFFFFBA, 0xEFFFBA, 0xDFFFBA, 0xCAFFBA,
    0xBAFFBA, 0xBAFFCA, 0xBAFFDF, 0xBAFFEF,
    0xBAFFFF, 0xBAEFFF, 0xBADFFF, 0xBACAFF,
    // Through the rainbow 4 (dark)
    0x000071, 0x1C0071, 0x390071, 0x550071,
    0x710071, 0x710055, 0x710039, 0x71001C,
    0x710000, 0x711C00, 0x713900, 0x715500,
    0x717100, 0x557100, 0x397100, 0x1C7100,
    0x007100, 0x00711C, 0x007139, 0x007155,
    0x007171, 0x005571, 0x003971, 0x001C71,
    // Through the rainbow 5
    0x393971, 0x453971, 0x553971, 0x613971,
    0x713971, 0x713961, 0x713955, 0x713945,
    0x713939, 0x714539, 0x715539, 0x716139,
    0x717139, 0x617139, 0x557139, 0x457139,
    0x397139, 0x397145, 0x397155, 0x397161,
    0x397171, 0x396171, 0x395571, 0x394571,
    // Through the rainbow 6
    0x515171, 0x595171, 0x615171, 0x695171,
    0x715171, 0x715169, 0x715161, 0x715159,
    0x715151, 0x715951, 0x716151, 0x716951,
    0x717151, 0x697151, 0x617151, 0x597151,
    0x517151, 0x517159, 0x517161, 0x517169,
    0x517171, 0x516971, 0x516171, 0x515971,
    // Through the rainbow 7 (extra dark)
    0x000041, 0x100041, 0x200041, 0x310041,
    0x410041, 0x410031, 0x410020, 0x410010,
    0x410000, 0x411000, 0x412000, 0x413100,
    0x414100, 0x314100, 0x204100, 0x104100,
    0x004100, 0x004110, 0x004120, 0x004131,
    0x004141, 0x003141, 0x002041, 0x001041,
    // Through the rainbow 8
    0x202041, 0x282041, 0x312041, 0x392041,
    0x412041, 0x412039, 0x412031, 0x412028,
    0x412020, 0x412820, 0x413120, 0x413920,
    0x414120, 0x394120, 0x314120, 0x284120,
    0x204120, 0x204128, 0x204131, 0x204139,
    0x204141, 0x203941, 0x203141, 0x202841,
    // Through the rainbow 9
    0x2D2D41, 0x312D41, 0x352D41, 0x3D2D41,
    0x412D41, 0x412D3D, 0x412D35, 0x412D31,
    0x412D2D, 0x41312D, 0x41352D, 0x413D2D,
    0x41412D, 0x3D412D, 0x35412D, 0x31412D,
    0x2D412D, 0x2D4131, 0x2D4135, 0x2D413D,
    0x2D4141, 0x2D3D41, 0x2D3541, 0x2D3141
  );

  /**
   * The colors from the (sun's) spectrum.
   *
   * Note that these spectral colors are only an approximation since the
   * real spectral colors cannot all be displayed by a computer monitor.
   *
   * Source: http://en.wikipedia.org/wiki/Visible_spectrum
   */
  final val _rainbow_colors = Array(
    0x020003, 0x050008, 0x090010, 0x0D0018,
    0x11001F, 0x150027, 0x19002E, 0x1E0035,
    0x22003C, 0x260044, 0x2A004B, 0x2E0052,
    0x320059, 0x360060, 0x380066, 0x3A006B,
    0x3D0070, 0x3E0075, 0x40007B, 0x430080,
    0x450085, 0x47008B, 0x490090, 0x4B0095,
    0x4D009A, 0x5000A0, 0x5000A4, 0x4F00A8,
    0x4F00AC, 0x4E00B0, 0x4E00B4, 0x4D00B7,
    0x4D00BB, 0x4C00BF, 0x4C00C3, 0x4C00C7,
    0x4B00CB, 0x4A00CE, 0x4A00D2, 0x4800D5,
    0x4500D6, 0x4300D8, 0x4001DA, 0x3D01DB,
    0x3B01DD, 0x3801DE, 0x3501E0, 0x3301E2,
    0x3002E4, 0x2D02E6, 0x2B02E7, 0x2802E9,
    0x2606EA, 0x240BEB, 0x220FED, 0x2014EE,
    0x1E18EF, 0x1C1CF0, 0x1A21F1, 0x1825F2,
    0x162AF4, 0x142EF5, 0x1232F6, 0x1037F7,
    0x0E3CF8, 0x0D42F6, 0x0C48F5, 0x0B4FF3,
    0x0A55F2, 0x095CF1, 0x0862EF, 0x0769EE,
    0x066FEC, 0x0576EB, 0x047CEA, 0x0382E8,
    0x0289E7, 0x018FE4, 0x0194DE, 0x0199D9,
    0x019ED3, 0x01A3CE, 0x01A8C9, 0x00AEC3,
    0x00B2BE, 0x00B8B8, 0x00BDB3, 0x00C2AD,
    0x00C7A7, 0x00CCA2, 0x00CE9C, 0x00CE95,
    0x00CF8E, 0x00CF88, 0x00CF81, 0x00D07A,
    0x00D074, 0x00D06D, 0x00D166, 0x00D15F,
    0x00D159, 0x00D252, 0x00D24C, 0x00D146,
    0x00CF42, 0x00CD3D, 0x00CC37, 0x00CA33,
    0x00C82E, 0x00C62A, 0x00C425, 0x00C320,
    0x00C11B, 0x00BF16, 0x00BE12, 0x00BC0D,
    0x00BC0C, 0x00BB0B, 0x00BB0A, 0x00BA09,
    0x00BA08, 0x00BA07, 0x00B906, 0x00B905,
    0x00B804, 0x00B803, 0x00B802, 0x00B701,
    0x00B700, 0x00B701, 0x00B802, 0x00B803,
    0x00B904, 0x00B905,
    0x00B906, 0x00BA07, 0x00BA09, 0x00BB09,
    0x00BB0A, 0x00BB0C, 0x00BC0C, 0x03BD0D,
    0x0CBF0B, 0x13C00B, 0x1BC20A, 0x22C308,
    0x2AC508, 0x32C706, 0x3AC906, 0x42CA05,
    0x4ACC03, 0x52CE02, 0x5AD001, 0x62D100,
    0x69D200, 0x70D200, 0x76D300, 0x7DD300,
    0x84D300, 0x8AD400, 0x91D400, 0x97D400,
    0x9ED500, 0xA5D500, 0xABD500, 0xB2D600,
    0xB9D600, 0xBED400, 0xC2D100, 0xC8CF00,
    0xCDCD00, 0xD2CA00, 0xD7C800, 0xDBC600,
    0xE1C300, 0xE5C100, 0xEBBE00, 0xEFBC00,
    0xF4BA00, 0xF8B700, 0xF9B200, 0xFAAE00,
    0xFBA900, 0xFBA400, 0xFB9F00, 0xFC9A00,
    0xFC9500, 0xFD9100, 0xFD8C00, 0xFE8700,
    0xFE8200, 0xFF7E00, 0xFF7900, 0xFF7500,
    0xFF7100, 0xFF6C00, 0xFF6800, 0xFF6400,
    0xFF6000, 0xFF5B00, 0xFF5700, 0xFF5300,
    0xFF4F00, 0xFF4B00, 0xFF4600, 0xFF4300,
    0xFF4100, 0xFF3E00, 0xFF3B00, 0xFF3900,
    0xFF3700, 0xFF3401, 0xFF3201, 0xFF3001,
    0xFF2D01, 0xFF2B01, 0xFF2801, 0xFF2601,
    0xFD2401, 0xF92201, 0xF52001, 0xF11E01,
    0xEE1D01, 0xEA1B01, 0xE61900, 0xE31700,
    0xE01500, 0xDB1400, 0xD81200, 0xD51000,
    0xD00D00, 0xCB0D00, 0xC40C00, 0xBE0B00,
    0xB70B00, 0xB00900, 0xAA0900, 0xA30800,
    0x9C0700, 0x950600, 0x8F0500, 0x880400,
    0x810300, 0x7A0200, 0x720200, 0x680200,
    0x5E0200, 0x540100, 0x4B0100, 0x410100,
    0x370100, 0x2E0100, 0x250100, 0x1B0000,
    0x110000, 0x070000
  );

  def colorFromCMY(c: Double, m: Double, y: Double): java.awt.Color = {
    val r = 1 - c
    val g = 1 - m
    val b = 1 - y
    new java.awt.Color(r.toFloat, g.toFloat, b.toFloat)
  }

  def colorFromCMYK(c: Double, m: Double, y: Double, k: Double): java.awt.Color = {
    val r = (1 - c) * (1 - k)
    val g = (1 - m) * (1 - k)
    val b = (1 - y) * (1 - k)
    new java.awt.Color(r.toFloat, g.toFloat, b.toFloat)
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

  def colorFromRGB(r: Double, g: Double, b: Double, a: Double): java.awt.Color = {
    val red = ((r max 0) * scaling).toInt min 255
    val green = ((g max 0) * scaling).toInt min 255
    val blue = ((b max 0) * scaling).toInt min 255
    val alpha = ((a max 0) * scaling).toInt min 255
    new java.awt.Color(red, green, blue, alpha)
  }

  def colorFromString(value: String): java.awt.Color =
    _colorNames.get(value.toLowerCase.filter(_ != ' ')) match {
      case Some(value) =>
        new java.awt.Color(value)
      case None =>
        val color = javafx.scene.paint.Color.valueOf(value)
        val r = (color.getRed * 255).toInt
        val g = (color.getGreen * 255).toInt
        val b = (color.getBlue * 255).toInt
        new java.awt.Color(r, g, b)
    }

  /**
   * This function returns the indexed color from the given color-array.
   */
  private def getIndexedColor(colorList: Array[Int], index: Double) = {
    val idx =
      if (index <= 0) (-index).toInt
      else (index * colorList.length + 0.5).toInt
    if (idx >= 0 && idx < colorList.length)
      new java.awt.Color(colorList(idx))
    else
      null
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

  def makeColor(arg: String): PyColor =
    new PyColor(colorFromString(arg))

  def makeColor(arg1: PyObject, arg2: PyObject): PyColor =
    (arg1, arg2) match {
      case (color: PyColor, alpha: PyFloat) =>
        val c = new PyColor(color)
        c.setAlpha(alpha.asDouble().toFloat)
        c
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

  def makeColor(arg1: PyObject, arg2: PyObject, arg3: PyObject): PyColor = {
    if ((arg1.isInstanceOf[PyFloat] || arg1.isInstanceOf[PyInteger]) &&
        (arg2.isInstanceOf[PyFloat] || arg2.isInstanceOf[PyInteger]) &&
        (arg3.isInstanceOf[PyFloat] || arg3.isInstanceOf[PyInteger]))
        createColor("rgb", arg1, arg2, arg3)
    else
      null
  }

  def makeColor(arg1: PyObject, arg2: PyObject, arg3: PyObject, arg4: PyObject): PyColor = {
    if ((arg1.isInstanceOf[PyFloat] || arg1.isInstanceOf[PyInteger]) &&
      (arg2.isInstanceOf[PyFloat] || arg2.isInstanceOf[PyInteger]) &&
      (arg3.isInstanceOf[PyFloat] || arg3.isInstanceOf[PyInteger]) &&
      (arg4.isInstanceOf[PyFloat] || arg4.isInstanceOf[PyInteger]))
      createColor("rgba", arg1, arg2, arg3, arg4)
    else
      null
  }

  protected def createColor(colormode: String, values: PyObject*): PyColor =
    colormode.toLowerCase match {
      case "cmy" =>
        if (values.length == 3)
          new PyColor(colorFromCMY(values(0).asDouble(), values(1).asDouble, values(2).asDouble()))
        else
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
        if (values.length == 3)
          new PyColor(colorFromRGB(values(0).asDouble(), values(1).asDouble,
            values(2).asDouble(), values(3).asDouble()))
        else
          null
      case "rainbow" | "spectrum" =>
        new PyColor(getIndexedColor(_rainbow_colors, values(0).asDouble()))
      case "vga" =>
        new PyColor(getIndexedColor(_vgaColors, values(0).asDouble()))
      case color =>
        if (values.isEmpty)
          makeColor(color)
        else if (values.length == 1)
          makeColor(makeColor(color), values(0))
        else
          null
    }

  protected def mixColors(color1: PyColor, color2: PyColor): PyColor = {
    val red = (color1.getRed + color2.getRed) / 2
    val green = (color1.getGreen + color2.getGreen) / 2
    val blue = (color1.getBlue + color2.getBlue) / 2
    new PyColor(colorFromRGB(red, green, blue))
  }
}
