# Writing Plugins for TigerJython

TigerJython support simple plugins, written in Python (executed by the internal Jython interpreter).  Once TigerJython is started, it looks for a file `plugins/__init__.py`.  If it finds such a file (either inside the JAR, or in the same directory where the JAR resides), it executes the respective file.  It is good style, though, to put your plugin into a separate file inside the `plugins` folder, and then add an import to `__init__.py`.

Plugins are initialised once the entire user interface is set up, i.e. the script `__init__.py` is executed once the entire mainwindow is available.  Although your plugin will probably just to some registering of components at this time.

To allow plugins to interact with TigerJython itself, TigerJython provides an interface called `MainWindow`, which can be obtained through the method `tigerjython.core.TigerJython.getMainWindow()` as shown below.  There is only one global `MainWindow` object in the system, which means it does not matter whether you cache it inside your Python code, or obtain it again later on.

Additionally, there is an event manager for plugins, which can be obtained through `tigerjython.core.TigerJython.getEventManager()`.  It allows to register listeners for various kinds of events as shown below.  Note that these events do not allow your plugin to directly interfere with TigerJython's reactions.  They are rather simple notifications of an event and the time it has occurred.  The idea behind these events is to support research on user reaction by monitoring key strokes, etc.

Although TigerJython only supports plugins written in Python, you can also add Java-based plugins as Jython allows you to call any Java method available.



## Examples

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

Monitoring key events requires to write a Python function with the correct number of arguments as noted below, and registering the function as a listener.
```python
from tigerjython.core import TigerJython

def onKeyPressed(time, pos, key):
    ... # record the key

eventManager = TigerJython.getEventManager()
eventManager.addOnKeyPressedListener(onKeyPressed)
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



## Event-Manager

The `EventManager` interface currently supports the following listeners.  All events provide a time variable in _milliseconds_, which is based on Java's `System.currentTimeMillis()` function.  Although there is no guarantee that the accuracy of one millisecond can be met, the given timings should accurately reflect the order of events.

- `onError(time: Long, line: Int, column: Int, msg: String)` is fired when either a syntax or a runtime error occurred and is displayed to the user.  Besides the message, the notification also provides the line and column of where the error occurred&mdash;if available (some errors do not have a meaningful value for column, in which case it is just set to zero).
- `onKeyPressed(time: Long, pos: Int, key: String)` is fired when the user presses a key that modifies the text (i.e. a character, delete, etc), or moves the caret (text-cursur) such as arrows or page up/down.  For simple characters the `key` argument contains the character directly.  Other key types are expressed as uppercase identifiers, such as `LEFT`, `UP`, `ENTER`, `BACK_SPACE`, `DELETE` etc. (following the naming of JavaFX's `javafx.scene.input.KeyEvent`).  In addition to the key, the event listener also receives the position of the caret (text-cursor) at the time of the keystroke.
- `onRun(time: Long)` is fired when the user clicks `run` to execute the current program.  Since the program will usually be checked for static syntax errors before actual execution, the occurrence of this event does not yet mean that the program is actually running.
- `onStarted(time: Long)` is fired when the program has actually started executing and is running.
- `onStopped(time: Long)` is fired when the program has stopped running.

Use the following methods to add or remove a specific listener:

- `EventManager.addOnErrorListener(listener)`, `EventManager.removeOnErrorListener(listener)`
- `EventManager.addOnKeyPressedListener(listener)`, `EventManager.removeOnKeyPressedListener(listener)`
- `EventManager.addOnRunListener(listener)`, `EventManager.removeOnRunListener(listener)`
- `EventManager.addOnStartedListener(listener)`, `EventManager.removeOnStartedListener(listener)`
- `EventManager.addOnStoppedListener(listener)`, `EventManager.removeOnStoppedListener(listener)`

The listeners themselves are of types: [`tigerjython.plugins.ErrorNotification`](src/main/scala/tigerjython/plugins/ErrorNotification.scala), [`tigerjython.plugins.KeyPressNotification`](src/main/scala/tigerjython/plugins/KeyPressNotification.scala), abd [`tigerjython.plugins.TimedNotification`](src/main/scala/tigerjython/plugins/TimedNotification.scala), respectively.

