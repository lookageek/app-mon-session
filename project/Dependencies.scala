import sbt._

object Dependencies {
    val tapirVersion = "1.8.2"
    val circeVersion = "0.14.2"
    val list = List(
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
        "ch.qos.logback" % "logback-classic" % "1.2.10",
        "com.softwaremill.sttp.tapir" %% "tapir-netty-server" % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
        "io.circe" %% "circe-core" % circeVersion,
        "io.circe" %% "circe-generic" % circeVersion,
        "org.http4s" %% "http4s-ember-server" % "0.23.18"
    )
}
