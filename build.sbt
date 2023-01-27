enablePlugins(ScalaJSPlugin)
enablePlugins(ScalaJSBundlerPlugin)
name := "miniScribe"
scalaVersion := "3.2.1"

// This is an application with a main method
scalaJSUseMainModuleInitializer := true

libraryDependencies += "com.softwaremill.sttp.client3" %%% "core" % "3.8.9"
libraryDependencies += "org.scala-lang.modules" %%% "scala-xml" % "2.1.0"
scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }

npmDependencies in Compile += "@zip.js/zip.js" -> "~2.6.62"
