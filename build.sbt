/**
 *  This is the build-file for the _TigerJython_ Python environment.
 *
 *  (c) 2020-2021, Tobias Kohn
 */

// This function creates a `BuildInfo` object to make version and build information accessible to the application
// at runtime.
def generateBuildInfo(packageName: String,
                      objectName: String = "BuildInfo"): Setting[_] =
  Compile / sourceGenerators += Def.task {
    val file =
      packageName
        .split('.')
        .foldLeft((Compile / sourceManaged).value)(_ / _) / s"$objectName.scala"
        
    IO.write(
      file,
      s"""package $packageName
         |
         |object $objectName {
         |  val Name = "${name.value}"
         |  val Version = "${version.value}"
         |  val Tag = "${buildTag}"
         |  val Build = "${buildVersion}"
         |  val Date = "${buildDate}"
         |
         |  def fullVersion: String = 
         |    "%s.%s%s (%s)".format(Version, Build, Tag, Date)
         |}""".stripMargin
    )

    Seq(file)
  }.taskValue
  
// Information about the application itself
name := "TigerJython3"

version := "3.0"

// We use a recent version of Scala
scalaVersion := "2.13.2"

// Unfortunately, we need to limit the Java version as Java 8 is still fairly common
javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

// Actual build information, such as the date of building the application
val currentDate = java.time.LocalDate.now
val buildDate = "%d %s %d".format(
  currentDate.getDayOfMonth, 
  currentDate.getMonth.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH), 
  currentDate.getYear
)

val buildTag = "-SNAPSHOT"

val buildVersion = "ea+12"

// This is needed to run/test the project without having to restart SBT afterwards
run / fork := true

// Installing the correct JavaFX libraries is OS-dependent...
val osName: SettingKey[String] = SettingKey[String]("osName")

osName := (System.getProperty("os.name") match {
  case name if name.startsWith("Linux") => "linux"
  case name if name.startsWith("Mac") => "mac"
  case name if name.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
})

// We need the newest version as it contains important fixes for Mac OS X
// Actually, version 17.0.1 is out but due to a bug in JavaFX maven the current version of SBT cannot pull it :-(
val fxVersion = "16" //  "11-ea+25"

/*libraryDependencies += "org.openjfx" % "javafx-base" % fxVersion classifier osName.value
libraryDependencies += "org.openjfx" % "javafx-controls" % fxVersion classifier osName.value
libraryDependencies += "org.openjfx" % "javafx-fxml" % fxVersion classifier osName.value
libraryDependencies += "org.openjfx" % "javafx-graphics" % fxVersion classifier osName.value*/

// Get the necessary libraries for all platforms
// (most of all, we want to make sure that the assembled JAR contains all necessary files)
libraryDependencies += "org.openjfx" % "javafx-base" % fxVersion classifier "linux"
libraryDependencies += "org.openjfx" % "javafx-controls" % fxVersion classifier "linux"
libraryDependencies += "org.openjfx" % "javafx-fxml" % fxVersion classifier "linux"
libraryDependencies += "org.openjfx" % "javafx-graphics" % fxVersion classifier "linux"

libraryDependencies += "org.openjfx" % "javafx-base" % fxVersion classifier "win"
libraryDependencies += "org.openjfx" % "javafx-controls" % fxVersion classifier "win"
libraryDependencies += "org.openjfx" % "javafx-fxml" % fxVersion classifier "win"
libraryDependencies += "org.openjfx" % "javafx-graphics" % fxVersion classifier "win"

libraryDependencies += "org.openjfx" % "javafx-base" % fxVersion classifier "mac"
libraryDependencies += "org.openjfx" % "javafx-controls" % fxVersion classifier "mac"
libraryDependencies += "org.openjfx" % "javafx-fxml" % fxVersion classifier "mac"
libraryDependencies += "org.openjfx" % "javafx-graphics" % fxVersion classifier "mac"

// In order to include OpenJFX for ARM architecture, we need at least version 17.  However, due to a bug it is currently
// not possible.  We have to wait for updates to SBT/Maven before being able to pull the correct versions.
/* libraryDependencies += "org.openjfx" % "javafx-base" % fxVersion classifier "linux-aarch64"
libraryDependencies += "org.openjfx" % "javafx-controls" % fxVersion classifier "linux-aarch64"
libraryDependencies += "org.openjfx" % "javafx-fxml" % fxVersion classifier "linux-aarch64"
libraryDependencies += "org.openjfx" % "javafx-graphics" % fxVersion classifier "linux-aarch64" */

// Other dependencies
libraryDependencies += "org.fxmisc.richtext" % "richtextfx" % "0.10.5"

// We switch to our internal version of Jython
// libraryDependencies += "org.python" % "jython-standalone" % "2.7.2"

// When building a common JAR, some files are typically shared and we need to discard
// any superfluous files
assembly / assemblyMergeStrategy := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}

assembly / assemblyJarName := s"TigerJython3-${buildVersion}.jar"

// Update build info (like app name, version, etc.)
generateBuildInfo("tigerjython.core")
