
lazy val commonSettings = Seq(
  name := "Nostalgia",
  scalaVersion := "2.12.9",
  organization := "com.melvic",
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
  //addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
)

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")

//lazy val scalaReflect = Def.setting { "org.scala-lang" % "scala-reflect" % scalaVersion.value }

lazy val root = (project in file("."))
  .aggregate(core/*, macros*/)
  .settings(commonSettings)

lazy val core = (project in file("core"))
  //.dependsOn(macros)
  .settings(commonSettings,
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "11-R16",
      "org.typelevel" %% "cats-core" % "2.0.0"
    ),
    libraryDependencies ++= javaFXModules.map(m =>
      "org.openjfx" % s"javafx-$m" % "11" classifier osName))

/*lazy val macros = (project in file("macros"))
  .settings(
    commonSettings,
    libraryDependencies += scalaReflect.value)*/
