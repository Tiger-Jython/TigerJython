package tigerjython.jython

import org.python.core.{Py, PyObject}

import javax.swing.JOptionPane
import scala.annotation.tailrec
import scala.util.control.Exception.allCatch

/**
 * Functions for Input and Output such as `input()` and `msgDlg()`.
 *
 * @author Tobias Kohn
 */
object IOFunctions {

  private def _displayMessage(message: String): Unit =
    JOptionPane.showMessageDialog(null, message)

  private def _requestInput(prompt: String): String =
    JOptionPane.showInputDialog(prompt)

  private def _isInteger(inputText: String): Boolean =
    (inputText.nonEmpty && inputText.forall(_.isDigit)) || (inputText.length >= 2 &&
      (((c: Char) => c == '+' || c == '-')(inputText.head) && inputText.tail.forall(_.isDigit)))

  /**
   * Our own version of `input()` returns either an integer, a floating point number or a string, depending on the
   * actual value entered.
   */
  def input(prompt: String): PyObject = {
    val inputText = _requestInput(prompt)
    if (inputText != null) {
      if (_isInteger(inputText)) {
        if (inputText.length < 9)
          Py.newInteger(inputText.toInt)
        else
          Py.newLong(inputText)
      } else
        allCatch opt inputText.toDouble match {
          case Some(value) =>
            Py.newFloat(value)
          case None =>
            Py.newString(inputText)
        }
    } else
      Py.None
  }

  @tailrec
  def _inputFloat(prompt: String, showError: Boolean): PyObject = {
    val inputText = _requestInput(prompt)
    if (inputText != null)
      allCatch opt inputText.toDouble match {
        case Some(value) =>
          Py.newFloat(value)
        case None =>
          _inputFloat(prompt, showError = true)
      }
    else
      Py.None
  }

  def inputFloat(prompt: String): PyObject = _inputFloat(prompt, showError = false)

  @tailrec
  private def _inputInt(prompt: String, showError: Boolean): PyObject = {
    val inputText = _requestInput(prompt)
    if (inputText != null) {
      if (_isInteger(inputText)) {
        if (inputText.length <= 9)
          Py.newInteger(inputText.toInt)
        else
          Py.newLong(inputText)
      } else
        _inputInt(prompt, showError = true)
    } else
      Py.None
  }

  def inputInt(prompt: String): PyObject = _inputInt(prompt, showError = false)

  def inputString(prompt: String): PyObject = {
    val result = _requestInput(prompt)
    if (result != null)
      Py.newString(result)
    else
      Py.None
  }

  def msgDlg(message: Array[PyObject]): Unit = {
    val s = message.map(_.asString()).mkString(" ")
    _displayMessage(s)
  }
}
