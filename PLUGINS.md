# Writing Plugins for TigerJython

TigerJython support simple plugins, written in Python (executed by the internal Jython interpreter).  Once TigerJython is started, it looks for a file `plugins/__init__.py`.  If it finds such a file (either inside the JAR, or in the same directory where the JAR resides), it executes the respective file.  It is good style, though, to put your plugin into a separate file inside the `plugins` folder, and then add an import to `__init__.py`.

Plugins are initialised once the entire user interface is set up, i.e. the script `__init__.py` is executed once the entire mainwindow is available.  Although your plugin will probably just to some registering of components at this time.

To allow plugins to interact with TigerJython itself, TigerJython provides an interface called `MainWindow`, which can be obtained through the method `tigerjython.core.TigerJython.getMainWindow()` as shown below.  There is only one global `MainWindow` object in the system, which means it does not matter whether you cache it inside your Python code, or obtain it again later on.

Most plugins provide an entry in the menu so that they can be invoked.  The following example demonstrates how the `__init__.py` script can get a reference to the `MainWindow` interface to install a new entry in the `Tools` menu.
```python
from tigerjython.core import TigerJython

def greetings():
    mainWindow.alert("Greetings from your plugin!")

mainWindow = TigerJython.getMainWindow()
mainWindow.addMenuItem("tools.test", "Greet me!", greetings)
```

This example demonstrates some basic interaction.  Note that you should use `MainWindow.input(prompt)` rather than the built-in `input()` function, as the latter may not be integrated with the overall user interface (this is in part an issue of JavaFX vs AWT/Swing).
```python
from tigerjython.core import TigerJython

mainWindow = TigerJython.getMainWindow()
name = mainWindow.input("Who are you?")
mainWindow.setSelectedText("Hello " + name)
```


## Main-Window

The `MainWindow` interface currently provides the following methods:

- `addMenuItem(name: String, caption: String, action: java.lang.Runnable)` installs a new entry in the menubar.  The name specifies a name, which can contain a prefix such as `"tools."` to install the entry in the `Tools` menu.  TigerJython replaces the dots by hyphens and uses the resulting name as id of the created menu item (e.g., `"tools.test"` becomes `"tools-test"`), which allows you to style it through CSS.  The caption is displayed to the user and the action can actually be any Python function that does not take any argument.
- `alert(msg: String)` opens a little dialog box to show the given string along with an `OK` button.
- `confirm(msg: String)` opens a little dialog box to show the given string along with an `OK` and a `Cancel` button.
- `getCaretPosition(): Int` returns the position of the caret (text-cursor) within the text.  If no editor is active, the returned value is `-1`.
- `getSelectedText(): String` returns the current selected text in the editor, or `None` if no editor is active at the moment.
- `getText(): String` returns the full text in the editor, or `None` if no editor is active at the moment.
- `input(prompt: String): String` displays a dialog box for the user to enter some text and returns it as a string.  If the user cancels the input, `None` is returned instead.
- `setSelectedText(text: String)` replaces the currently selected text in the editor by the new text.  If no text is selected, the new text is inserted at the position of the caret.

