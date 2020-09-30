# Changelog


### 3.0 Early Access Preview 7 (30 September 2020)

Implemented:
- Can delete files from the '+' tab;
- TigerJython executor instances are started ahead of time for faster response;
- Saves all documents so that they can be mutually imported;

Changes:
- Import of files from disk is more prominent and visible;

Bug fixes:
- Loads previously created files;
- All threads and running executor instances are shutdown when closing the app;
- Runtime errors are actually displayed;

Known issues:
- Errors in modules are not displayed at the correct position;


### 3.0 Early Access Preview 6 (25 September 2020)

Implemented:
- Simple notebooks to work with Python interactively;
- Undo history of documents is saved and restored across sessions;
- Added icons to choose the interpreter and language;
- Can change the name/caption of a document;
- Can 'download' programs to external files;

Changes:
- The executing Python interpreter is now chosen on the basis of individual editor tabs rather than
  globally.  This works better for additional target devices where fast switching is necessary;
- The traditional menu is no longer necessary and superseded by options that directly embedded into
  the UI;

Bug fixes:
- Correctly handles text sizes beyond 8 KB;

Known issues:
- Starting a TigerJython instance for execution is slow and should be done ahead of time to make it 
  more responsive;
- Documents should not be synchronised with files on the disc automatically;
- TigerJython's special functions are not yet fully supported;
- Missing support for executing programs on external devices; 


### 3.0 Early Access Preview 5 (30 May 2020)

Implemented:
- Support for `repeat` loops;
- TigerJython specific libraries (only as part of the release, no sources);
- New file management that allows reopening previously edited files;
- "+"/"Add" tab to create a new document or reopen a previous one;

Big fixes:
- Error messages on JRE 8 are displayed too large;


### 3.0 Early Access Preview 3 (25 May 2020)

Implemented:
- Plugins can listen to various events for recording user action;
- Add custom Python interpreter;
- Syntax checker honours Python version and other preferences;
- Customise appearance with font, font-size, and themes;

Bug fixes:
- Line numbers on JRE 8 were not displayed correctly;


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
- Basic plugins.

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
