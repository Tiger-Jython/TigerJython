# Changelog


### 3.0 Early Access Preview 1 (19 May 2020)

_This is a very early access release with most features not yet implemented._

Implemented:
- Execute Python code with the built-in Jython interpreter;
- Execute Python code with an external Python interpreter;
- Detect Python interpreters installed on the system;
- Save files automatically after about two seconds of no user input;
- Change the language in the system;
- Display error messages;
- Perform static error checking of the Python programs;
- Persistent storage of preferences and settings (not all settings are fully implemented, yet);
- Jython is included as a dependency from Maven central, i.e. updating it to the newest version takes a few seconds;
- Plugins.

Still missing:
- Syntax highlighting;
- Support for `repeat` loops;
- TigerJython-specific libraries;
- Full customisation of the interface (such as selecting font family or themes);
- Interaction with the server (check for updates, send usage statistics);
- Adding custom Python interpreters (that are not detected by TigerJython);
- Interctive concole and/or notebooks.


## Changes From Version 2

TigerJython 3 is a complete reimplementation as compared to Version 2.X.

- Replace _Swing_ by _JavaFX_ as the graphic framework for the user interface.
- Support other Python interpreters than Jython to execute code.
- Use CSS to style the application and allow for different themes.
- Do not require saving files but do so automatically in frequent intervals.
- Run the executing interpreter in a separate process.
- Better scaling for screens with high pixel density.
- Open source the project.
