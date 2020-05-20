/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.jython;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import org.python.core.Py;
import org.python.core.PyObject;

/**
 * Since Scala has no static methods, we need to define the static methods for built-in replacements inside a
 * Java class.
 *
 * TODO: we need to properly initialise JavaFX in order to use these JavaFX controls as below.  Otherwise we just
 * get exceptions back.
 *
 * @author Tobias Kohn
 */
public class Builtins {

    public static PyObject raw_input() {
        return raw_input("");
    }

    public static PyObject raw_input(String prompt) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setHeaderText(prompt);
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent())
            return Py.newString(result.get());
        else
            return Py.None;
    }

    public static PyObject input() {
        return input("");
    }

    public static PyObject input(String prompt) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setHeaderText(prompt);
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent())
            return Py.newString(result.get());
        else
            return Py.None;
    }

    public static PyObject msgDlg(PyObject message) {
        Alert alert = new Alert(AlertType.NONE, message.toString(), ButtonType.OK);
        alert.showAndWait();
        return Py.None;
    }
}
