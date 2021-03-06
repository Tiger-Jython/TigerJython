/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.jython;

import javax.swing.JOptionPane;

import org.python.core.*;

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

    public static PyObject isInteger(PyObject obj) {
        if (obj instanceof PyInteger || obj instanceof PyLong)
            return Py.True;
        if (obj instanceof PyFloat) {
            double f = obj.asDouble();
            if ((Math.floor(f) == f) && !Double.isInfinite(f))
                return Py.True;
            else
                return Py.False;
        } else
            return Py.False;
    }

    public static PyObject msgDlg(PyObject message) {
        JOptionPane.showMessageDialog(null, message);
        return Py.None;
    }

    public static java.awt.Color makeColor(String value) {
        javafx.scene.paint.Color color = javafx.scene.paint.Color.valueOf(value);
        int r = (int)Math.round(color.getRed() * 255);
        int g = (int)Math.round(color.getGreen() * 255);
        int b = (int)Math.round(color.getBlue() * 255);
        return new java.awt.Color(r, g, b);
    }

    public static PyObject getTigerJythonFlag(String name) {
        return TigerJythonBuiltins.getTigerJythonSeting(name);
    }
}
