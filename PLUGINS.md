# Writing Plugins for TigerJython

TigerJython support simple plugins, written in Python (executed by the internal Jython interpreter).  Once TigerJython is started, it looks for a file `plugins/__init__.py`.  If it finds such a file (either inside the JAR, or in the same directory where the JAR resides), it executes the respective file.

To allow plugins to interact with TigerJython itself, TigerJython provides an interface called `MainWindow`, which can be obtained through the method `tigerjython.core.TigerJython.getMainWindow()` as shown below.

Most plugins provide an entry in the menu so that they can be invoked.  The following example demonstrates how the `__init__.py` script can get a reference to the `MainWindow` interface to install a new entry in the `Tools` menu.
```python
from tigerjython.core import TigerJython

def greetings():
    mainWindow.alert("Greetings from your plugin!")

mainWindow = TigerJython.getMainWindow()
mainWindow.addMenuItem("tools.test", "Greet me!", greetings)
```


## Main-Window

The `MainWindow` interface currently provides the following methods:

- `addMenuItem(name: String, caption: String, action: java.lang.Runnable)` installs a new entry in the menubar.  The name specifies a name, which can contain a prefix such as `"tools."` to install the entry in the `Tools` menu.  TigerJython replaces the dots by hyphens and uses the resulting name as id of the created menu item (e.g., `"tools.test"` becomes `"tools-test"`), which allows you to style it through CSS.  The caption is displayed to the user and the action can actually be any Python function that does not take any argument.
- `alert(msg: String)` opens a little dialog box to show the given string along with an `OK` button.
