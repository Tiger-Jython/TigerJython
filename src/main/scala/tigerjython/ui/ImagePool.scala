/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui

import javafx.scene.image.Image
import tigerjython.core.Configuration

/**
 * Contains a pool with images that are frequently used.
 *
 * @author Tobias Kohn
 */
object ImagePool {

  private val images = collection.mutable.Map[String, Image]()

  private def loadImage(name: String): Image = {
    val result = new Image(getClass.getClassLoader.getResourceAsStream("resources/%s_32.png".format(name)))
    images(name) = result
    result
  }

  private def loadLangImage(name: String): Image =
    new Image(getClass.getClassLoader.getResourceAsStream("resources/flag_%s.png".format(name)))

  lazy val app_Logo: Image = loadImage(Configuration.appLogoName)

  lazy val calliope_Logo: Image = loadImage("Calliope")

  lazy val microBit_Logo: Image = loadImage("MicroBit")

  lazy val jupyter_Logo: Image = loadImage("Jupyter")

  lazy val jython_Logo: Image = loadImage("Jython")

  lazy val tigerJython_Logo: Image = loadImage("TigerJython")

  lazy val pypy_Logo: Image = loadImage("PyPy")

  lazy val python_Logo: Image = loadImage("Python")

  lazy val python2_Logo: Image = loadImage("Python2")

  lazy val raspberryPi_Logo: Image = loadImage("RaspberryPi")

  lazy val flags: Map[String, Image] = {
    val f = collection.mutable.Map[String, Image]()
    for ((code, name) <- Configuration.availableLanguages) {
      val img = loadLangImage(code)
      if (img != null) {
        f(code) = img
        f(name) = img
      }
    }
    f.toMap
  }

  def byName(name: String): Image = {
    loadAll()
    images(name)
  }

  def loadAll(): Unit = {
    microBit_Logo
    jython_Logo
    tigerJython_Logo
    python_Logo
    python2_Logo
  }
}
