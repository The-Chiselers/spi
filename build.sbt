// See README.md for license details.

ThisBuild / scalaVersion     := "2.13.13"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "tech.rocksavage"
ThisBuild / organizationName := "Rocksavage Technology"

Test / parallelExecution := false

val chiselVersion   = "6.5.0"
val scalafmtVersion = "2.5.0"
lazy val root = (project in file("."))
  .settings(
    name                   := "gpio",
    Test / publishArtifact := true,
    libraryDependencies ++= Seq(
      "org.chipsalliance" %% "chisel"     % chiselVersion,
      "org.scalatest" %% "scalatest" % "3.2.16" % "test",
      
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-Ymacro-annotations"
    ),
    addCompilerPlugin(
      "org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full
    )
  )

// Scala coverage settings
// val build_root = sys.env("BUILD_ROOT")
// coverageDataDir            := target.value / s"../${build_root}/cov/scala"
// coverageFailOnMinimum      := true
// coverageMinimumStmtTotal   := 90
// coverageMinimumBranchTotal := 95
