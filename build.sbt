// See README.md for license details.

ThisBuild / scalaVersion     := "2.13.15"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "tech.rocksavage"
ThisBuild / organizationName := "Rocksavage Technology"

Test / parallelExecution := false

val chiselVersion   = "6.6.0"
val scalafmtVersion = "2.5.0"

lazy val chisel_module_runner = RootProject(
  uri("https://github.com/The-Chiselers/chisel_module_runner.git#main"),
)
lazy val stdlib = RootProject(
  uri("https://github.com/The-Chiselers/stdlib.git#main"),
)
lazy val synth = RootProject(
  uri("https://github.com/The-Chiselers/synth.git#main"),
)
lazy val test_utils = RootProject(
  uri("https://github.com/The-Chiselers/test_utils.git#main"),
)

lazy val root = (project in file("."))
  .settings(
    name                   := "spi",
    Test / publishArtifact := true,
    libraryDependencies ++= Seq(
      "org.chipsalliance" %% "chisel"     % chiselVersion,
      "edu.berkeley.cs"   %% "chiseltest" % "6.0.0" % "test"
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-Ymacro-annotations"
    ),
    addCompilerPlugin(
      "org.chipsalliance" % "chisel-plugin" % "6.6.0" cross CrossVersion.full
    ),
    coverageExcludedFiles := ".*GenVerilog;.*BaseParams;.*Interfaces"
  ).dependsOn(
      chisel_module_runner,
      stdlib,
      synth,
      test_utils,
    )

// Scala coverage settings
coverageDataDir            := target.value
coverageFailOnMinimum      := false
coverageMinimumStmtTotal   := 0
coverageMinimumBranchTotal := 0
