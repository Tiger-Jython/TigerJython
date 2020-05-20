package tigerjython.plugins

import java.io.{FileInputStream, InputStream}
import java.nio.file.Paths

import org.python.core.Options
import org.python.util.PythonInterpreter
import tigerjython.core.Configuration
import tigerjython.jython

/**
 * The Plugin-Manager loads any plugins.
 *
 * @author Tobias Kohn
 */
object PluginsManager {

  private class PythonInitializer(val resource: InputStream) extends Runnable {

    def run(): Unit = {
      Options.importSite = false
      Options.Qnew = true
      PythonInterpreter.initialize(System.getProperties, null, null)
      jython.JythonBuiltins.initialize()
      val interpreter = new PythonInterpreter()
      interpreter.execfile(resource, "plugins/__init__.py")
    }
  }

  def initialize(): Unit = {
    val resource = getClass.getClassLoader.getResourceAsStream("plugins/__init__.py")
    if (resource != null)
      new Thread(new PythonInitializer(resource)).start()
    // When dealing with path objects, we have to be careful:
    // If something fails, we do not want to crash the entire system.
    try {
      val extFile = Paths.get(Configuration.sourcePath).getParent.resolve("plugins/__init__.py").toFile
      if (extFile.exists()) {
        val stream = new FileInputStream(extFile)
        if (stream != null)
          new Thread(new PythonInitializer(stream)).start()
      }
    } catch {
      case _: Throwable =>
    }
  }
}
