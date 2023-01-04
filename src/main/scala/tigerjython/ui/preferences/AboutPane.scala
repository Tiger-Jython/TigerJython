/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.preferences

import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.{Label, ScrollPane}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{HBox, StackPane, VBox}
import javafx.scene.text.{Font, FontWeight, Text, TextFlow}
import tigerjython.core.BuildInfo
import tigerjython.ui.Weblink

/**
 *
 *
 * @author Tobias Kohn
 */
class AboutPane extends PreferencePane {

  val caption: StringProperty = new SimpleStringProperty("About")

  override lazy val node: Node = {
    val result = new VBox()
    val img = new Image(getClass.getClassLoader.getResourceAsStream("resources/splash.png"))
    val imgView = new ImageView(img)
    imgView.setScaleX(0.75)
    imgView.setScaleY(0.75)
    val imgBox = new HBox(imgView)
    imgBox.setAlignment(Pos.CENTER)
    result.getChildren.add(imgBox)
    result.getChildren.addAll(
      createHeading("TigerJython"),
      new Label("Version " + BuildInfo.fullVersion),
      new TextFlow(
        new Text("Copyright © 2012–2023, "),
        new Weblink("TJGroup", "https://tjgroup.ch/"),
      ),
      new TextFlow(
        new Text("Visit us on "),
        new Weblink("TigerJython Webpage", "https://tigerjython.ch/"),
        new Text(" or contact us on "),
        new Weblink("info@tigerjython.ch", "mailto:info@tigerjython.ch")
      )
    )
    result.getChildren.addAll(
      new Label("   "),
      createHeading("3rd-party Projects"),
      new TextFlow(
        new Weblink("Jython", "https://www.jython.org/"),
        new Text("– the Python-implementation on the Java platform")
      ),
      new Weblink("RichTextFX", "https://github.com/FXMisc/RichTextFX")
    )
    result.getChildren.addAll(
      new Label("   "),
      createHeading("Sponsors"),
      new Weblink("Klett und Balmer", "https://www.klett.ch/")
    )
    result.setSpacing(4)
    new ScrollPane(new StackPane(result))
  }

  private def createHeading(text: String): Text = {
    val result = new Text(text)
    result.setFont(Font.font(null, FontWeight.BOLD, 20))
    result
  }
}
