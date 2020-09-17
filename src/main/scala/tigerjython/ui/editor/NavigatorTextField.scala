/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.editor

import java.lang

import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.control.TextField
import tigerjython.files.Documents

/**
 * The NavigatorTextField allows the user to change the name of a document.  However, there are some limitations on the
 * name: e.g., it must be unique and not be empty.  This text field makes sure this is satisfied.
 *
 * @author Tobias Kohn
 */
class NavigatorTextField extends TextField {

  private val existingNames = collection.mutable.Set[String]()

  private val _captionProperty: StringProperty = new SimpleStringProperty("")

  def captionProperty(): StringProperty = _captionProperty

  def getCaption: String = _captionProperty.get

  protected def isNamePermissible(name: String): Boolean = {
    !existingNames.contains(name) && !NavigatorTextField.commonLibraryNames.contains(name)
  }

  {
    getStyleClass.add("navigator-text")
  }

  focusedProperty().addListener(new ChangeListener[lang.Boolean] {
    override def changed(observableValue: ObservableValue[_ <: lang.Boolean],
                         oldValue: lang.Boolean, newValue: lang.Boolean): Unit = {
      if (newValue) {
        existingNames.clear()
        existingNames ++= Documents.getListOfDocumentNames
        existingNames.remove(captionProperty().get)
      } else {
        val t = textProperty().get()
        if (t == "" || !isNamePermissible(t))
          textProperty().set(captionProperty().get)
        else if (t != captionProperty().get)
          captionProperty().set(t)
      }
    }
  })
  captionProperty().addListener(new ChangeListener[String] {
    override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
      if (newValue != textProperty().get)
        textProperty().set(newValue)
    }
  })
}
object NavigatorTextField {

  private val commonLibraryNames = Set(
    "cmath",
    "gamegrid",
    "gpanel",
    "gturtle",
    "math",
    "random",
    "turtle"
  )
}