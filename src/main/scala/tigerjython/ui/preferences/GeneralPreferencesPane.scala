/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.preferences

import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.Node
import javafx.scene.control.{CheckBox, ComboBox, Label, ListCell}
import javafx.scene.image.ImageView
import javafx.scene.layout.{StackPane, VBox}
import javafx.scene.text.Font
import tigerjython.core.{Configuration, Preferences}
import tigerjython.ui.{ImagePool, UIString}

/**
 * The pane for general settings such as language or zoom level.  This is the one displayed first when the user opens
 * up the preferences.
 *
 * @author Tobias Kohn
 */
class GeneralPreferencesPane extends PreferencePane {

  val caption: StringProperty = new SimpleStringProperty("General")
  UIString("prefs.general") += caption

  protected def createLanguageChooser(): Seq[Node] = {
    val availableLanguages = Configuration.availableLanguages
    val currentLang = Preferences.language.get()
    var index: Int = availableLanguages.indexWhere(_._1 == currentLang)
    if (index < 0)
      index = availableLanguages.indexWhere(_._1 == "en") max 0
    val label = new Label("Language:")
    val chooser = new ComboBox[String]()
    label.setLabelFor(chooser)
    UIString("prefs.language") += label.textProperty()
    chooser.getItems.addAll( availableLanguages.map(_._2): _* )
    chooser.setButtonCell(new ImageListCell())
    chooser.setCellFactory(_ => {
      val result = new ImageListCell()
      result.fontProperty().bind(Preferences.defaultFont)
      result
    })
    chooser.getSelectionModel.select(index)
    chooser.valueProperty().addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
        val code = Configuration.getLanguageCode(newValue)
        Preferences.language.setValue(code)
      }
    })
    Seq(label, chooser)
  }

  protected def createServerElements(): Seq[Node] = {
    val updates = new CheckBox("Check for updates")
    val statistics = new CheckBox("Send anonymised usage statistics")
    UIString("prefs.checkupdate") += updates.textProperty()
    UIString("prefs.feedback.sendhttp") += statistics.textProperty()
    updates.selectedProperty().bindBidirectional(Preferences.checkUpdates)
    statistics.selectedProperty().bindBidirectional(Preferences.sendStatistics)
    Seq(updates, statistics)
  }

  protected def createZoomingElements(): Seq[Node] = {
    val zoomOptions = Seq[String](
      "50%", "75%", "90%", "100%", "110%", "125%", "150%", "200%", "250%"
    )
    val label = new Label("Zoom level:")
    val zoomChooser = new ComboBox[String]()
    label.setLabelFor(zoomChooser)
    UIString("prefs.zoom") += label.textProperty()
    zoomChooser.getItems.addAll( zoomOptions: _* )
    val currentValue = "%d%%".format(math.round(Preferences.globalZoom.get * 100))
    if (!zoomOptions.contains(currentValue))
      zoomChooser.getItems.add(0, currentValue)
    zoomChooser.getSelectionModel.select(currentValue)
    zoomChooser.valueProperty().addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], t: String, t1: String): Unit = {
        val factor = t1.dropRight(1).toDouble / 100
        Preferences.globalZoom.setValue(factor)
      }
    })
    Seq(label, zoomChooser)
  }

  override lazy val node: Node = {
    val result = new VBox()
    result.getChildren.addAll(createLanguageChooser(): _*)
    result.getChildren.addAll(createZoomingElements(): _*)
    result.getChildren.addAll(createServerElements(): _*)
    new StackPane(result)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private class ImageListCell extends ListCell[String] {
    override protected def updateItem(item: String, empty: Boolean): Unit = {
      super.updateItem(item, empty)
      val img = ImagePool.flags.get(item).orNull
      if (img != null)
        setGraphic(new ImageView(img))
      setText(item)
    }
  }
}
