name := "miniScribe"
scalaVersion := "3.2.1"

enablePlugins(ScalaJSPlugin)
enablePlugins(ScalaJSBundlerPlugin)
enablePlugins(ScalablyTypedConverterPlugin)

// This is an application with a main method
scalaJSUseMainModuleInitializer := true

libraryDependencies += "com.softwaremill.sttp.client3" %%% "core" % "3.8.9"
libraryDependencies += "org.gnieh" %%% "fs2-data-xml-scala" % "1.6.1"
libraryDependencies += "org.scala-lang.modules" %%% "scala-xml" % "2.1.0"
libraryDependencies += "de.tu-darmstadt.stg" %%% "rescala" % "0.32.0"
libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.12.0"

// js/ts dependencies
Compile / npmDependencies += "@zip.js/zip.js" -> "~2.6.62"
Compile / npmDependencies += "compression-webpack-plugin" -> "10.0.0"
// disable sourcemaps
Compile / fastOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) }
Compile / fullOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) }

// webpack config
useYarn := true
fullOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack.config.js")
// Export JSModule (needed for ScalaJSBundler)
scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
