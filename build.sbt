ThisBuild / scalaVersion := "2.12.16"

lazy val root = project
  .in(file("."))
  .settings(
    name := "App Monitoring",
    libraryDependencies ++= Dependencies.list,
  )