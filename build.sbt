import scalajsbundler.util.JSON.{obj, str}

name := "miniScribe"
scalaVersion := "3.3.1"

enablePlugins(ScalaJSPlugin)
enablePlugins(ScalaJSBundlerPlugin)
enablePlugins(ScalablyTypedConverterPlugin)

// This is an application with a main method
scalaJSUseMainModuleInitializer := true

// stricter compiler warnings
scalacOptions ++= List(
  "-deprecation",
  "-explain", // explain errors in more detail
  // "-new-syntax", // force new syntax
  // warn in case of unused imports and values
  "-Wunused:imports",
  "-Wunused:locals",
  "-Wunused:privates",
  "-Wunused:params",
  "-Wunused:implicits",
  "-Wunused:linted", // not sure what this does actually
  "-Xfatal-warnings", // turn warnings into errors
  "-Xmax-inlines:200" // needed for circe generic
)

libraryDependencies += "com.softwaremill.sttp.client3" %%% "core" % "3.9.0"
libraryDependencies += "org.gnieh" %%% "fs2-data-xml-scala" % "1.8.0"
libraryDependencies += "org.scala-lang.modules" %%% "scala-xml" % "2.2.0"
libraryDependencies += "de.tu-darmstadt.stg" %%% "rescala" % "0.33.0"
libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.12.0"

// js/ts dependencies
webpack / version := "5.76.2"
Compile / npmDependencies += "@zip.js/zip.js" -> "^2.6.62"
Compile / npmDependencies += "idb-keyval" -> "^6.2.1"
Compile / npmDevDependencies += "compression-webpack-plugin" -> "10.0.0"
// dependabot alerts
Compile / additionalNpmConfig ++= Map(
  "resolutions" -> obj(
    "ansi-html" -> str("^0.0.8"),
    "glob-parent" -> str("^5.1.2"),
    "node-forge" -> str("^1.3.0")
  )
)
// disable sourcemaps
Compile / fastOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) }
Compile / fullOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) }

// webpack config
useYarn := true
fullOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack.config.js")
// Export JSModule (needed for ScalaJSBundler)
scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }

// protocol buffers
// Compile / PB.targets := Seq(
//   scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
// )
// disable grpc service stub generation
Compile / PB.targets := Seq(
  scalapb.gen(grpc = false) -> (Compile / sourceManaged).value
)
