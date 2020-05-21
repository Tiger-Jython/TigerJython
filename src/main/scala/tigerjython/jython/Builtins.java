/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.jython;

import javax.swing.JOptionPane;
import org.python.core.Py;
import org.python.core.PyObject;

/**
 * Since Scala has no static methods, we need to define the static methods for built-in replacements inside a
 * Java class.
 *
 * @author Tobias Kohn
 */
public class Builtins {

    public static PyObject raw_input() {
        return raw_input("");
    }

    public static PyObject raw_input(String prompt) {
        String result = JOptionPane.showInputDialog(prompt);
        if (result != null)
            return Py.newString(result);
        else
            return Py.None;
    }

    public static PyObject input() {
        return input("");
    }

    public static PyObject input(String prompt) {
        PyObject eval = Py.getSystemState().builtins.__getitem__(Py.newString("eval"));
        String result = JOptionPane.showInputDialog(prompt);
        if (result != null) {
            if (eval != null)
                return eval.__call__(Py.newString(result));
            else
                return Py.newString(result);
        } else
            return Py.None;
    }

    public static PyObject msgDlg(PyObject message) {
        JOptionPane.showMessageDialog(null, message);
        return Py.None;
    }
}
