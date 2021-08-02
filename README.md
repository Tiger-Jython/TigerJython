# TigerJython

TigerJython is an educational development environment for Python with enhanced error messages.  It runs on the Java 
Virtual Machine and already includes [Jython](https://www.jython.org/).  If you have JRE installed, just download 
the [JAR-file](https://github.com/Tiger-Jython/TigerJython/releases/download/v3.0-ea%2B7/TigerJython3-ea+7.jar) under 
[**release**](https://github.com/Tiger-Jython/TigerJython/releases/latest).

> This is an early access version that is not yet ready for classroom use!!!

**Several features are not yet working, including syntax highlighting&mdash;there is no sense in raising issue tickets
at the moment, as we are constantly working on the IDE.**

**This version of TigerJython requires JRE version 9+ and will not run with JRE 8.**


## Features and Design

On the one hand, the overall design of the new TigerJython editor is based on our experience with previous versions of [TigerJython](http://jython.tobiaskohn.ch/) and [JEM](https://jythonmusic.me/).  On the other hand, we are moving to incorporate new features and update the user experience to reflect modern standards.

Some key features include:

- _Error messages:_ Enhanced error messages have always been one of the foremost core features of TigerJython and are also a crucial part of the new version.  In addition to sophisticated error detection, the error messages are available in a range of different languages (and new languages can be added at any time).
- _Batteries included:_ We adhere to Python's principle of "batteries included", which means that everything you need is already there.  In our case the Python interpreter to run your code&mdash;[Jython](https://www.jython.org/)&mdash;is part of the TigerJython JAR, together with all the libraries, bells and whistles.
- _Persistent user experience:_ With cloud computing and mobile apps that follow you everywhere you go, we are used to continue our work exactly where we left off.  The new version of TigerJython will honour this standard so that you won't need to search for your code you had been working on.
- _Simplicity:_ If you are looking for an IDE with powerful project management, TigerJython is not the IDE for you.  We stick to the very basics: write code and run it.
- _Customisability:_ As an educator or researcher you might be missing just the right functionality.  With the new plugin system, you can just add with a few simple lines of Python code.  For instance, simply install a new button that allows the students to direct submit their code to you.
- _Flexibility:_ While _Jython_ still is the backbone of _TigerJython_, you can equally use any other Python interpreter installed on your system.


## Plugins

You can write simple plugins for TigerJython.  The plugins are written in Python and run using the internal Jython interpreter.  On the one hand, plugins allow you to add additional tools and capabilities to TigerJython.  On the other hand, plugins also support research on how students learn programming in that you can easily write a plugin that monitors keystrokes.

More information can be found in the [Plugins Guide](PLUGINS.md).



## Publications

The _TigerJython_ IDE has been presented in the paper 
[Tell Me What's Wrong: A Python IDE with Error Messages](https://dl.acm.org/doi/abs/10.1145/3328778.3366920)
as published at the SIGCSE Technical Symposium 2020.  If you use TigerJython for academic work, please cite this paper:
```
@inproceedings{10.1145/3328778.3366920,
  author = {Kohn, Tobias and Manaris, Bill},
  title = {Tell Me What’s Wrong: A Python IDE with Error Messages},
  year = {2020},
  isbn = {9781450367936},
  publisher = {Association for Computing Machinery},
  address = {New York, NY, USA},
  url = {https://doi.org/10.1145/3328778.3366920},
  doi = {10.1145/3328778.3366920},
  booktitle = {Proceedings of the 51st ACM Technical Symposium on Computer Science Education},
  pages = {1054–1060},
  numpages = {7},
  keywords = {ide, compiler error messages, python},
  location = {Portland, OR, USA},
  series = {SIGCSE ’20}
}
```


## Build

TigerJython uses [`sbt`](https://www.scala-sbt.org/) to build the code.  Start `sbt` first and then use
`compile` to compile the code and `assembly` to generate a JAR file containing all the necessary 
libraries (such as Scala, Jython, etc).

By placing additional JARs into the `lib` subfolder, you can have additional files integrated into the
project.

If you want to use standard Jython, you might want to add the following line to `build.sbt`:
``libraryDependencies += "org.python" % "jython-standalone" % "2.7.2"``



## Structure

- **`/configparser:`**  a parser to read and process configuration files as used in TigerJython 2.  This is mostly for
  backward compatibility and to allow for manual configuration.  These configuration files are no longer used by
  TigerJython itself to save preferences.
- **`/core:`**  contains the object/method that is invoked when starting the application as well as handling of
  preferences and providing utilities to access system information.
- **`/errorhandling:`**  as the name says: specialised error handling for Python.
- **`/execute:`**  programs are no longer executed directly in the IDE process, but rather a new process is created
  in which the program is then run.  This allows to also target various interpreters installed on the system or even
  remote devices.
- **`/files:`**  the core of the storage system that replaces traditional file management.
- **`/jython:`**  TigerJython-specific extensions to Jython.  This will eventually be folded directly into a custom
  version of Jython.
- **`/microbit:`**  support for the BBC Micro:bit and the Calliope Mini.  The code for flashing the Microbit was
  heavily inspired by [Nicholas Tollervey's `uFlash`](https://github.com/ntoll/uflash), although with various 
  adaptations.
- **`/plugins:`**  the plugin support allowing to write and install plugins into the IDE.  The plugins themselves are
  written in Python (they may call into other Java-code, of course).
- **`/remote:`**  the engine to communicate with another instance of TigerJython.
- **`/syntaxsupport:`**  the syntax highlighter for Python code.  It works on several levels: first, the entire program
  is tokenised (split into tokens).  Second, the tokens are then grouped to form logical lines, allowing to determine
  scope, for instance.  Third, based on these logical lines, the input is parsed enough to extract relevant information 
  for syntax highlighting.  The parsing, however, is incomplete: its purpose is _not_ to find possible errors in the
  program code, but rather to determine the correct highlighting of names/identifiers in particular.  It also provides
  enough information about the program to extract imported modules or support auto inserting of closing parentheses and
  string delimiters.
- **`/ui:`**  the user interface and thus one of the core parts of TigerJython.
- **`/utils:`**  various helper tools.


## About

Who is behind TigerJython?  The first version of _TigerJython_ was written by Tobias Kohn, Aegidius Pluss and Jarka Arnold, together with extensive tutorials for learning and using Python, such as the one available on [TigerJython.ch](http://www.tigerjython.com/engl/index.php).  As it was quickly adopted throughout Europe, the team founded an non-profit association ("Verein" under Swiss law) [TigerJython-Group](http://tjgroup.ch/), which now officially maintains the IDE, the Python libraries and tutorials, and regularly develops and publishes new materials.

The team is also collaborating with institutions such as [ETH Zurich](https://www.abz.inf.ethz.ch/) and [PH Graubünden](https://phgr.ch/), leading to new developments, including the [Web Version of TigerJython](https://webtigerjython.ethz.ch/).

As we strongly believe that quality education must be accessible to everyone, we provide all our materials free of 
charge.  This is made possible not only through the great efforts of our members, but also thanks to our 
sponsor [Klett and Balmer Publishers](https://www.klett.ch/).
