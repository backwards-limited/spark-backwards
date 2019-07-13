import sbt._

object Dependencies {
  lazy val dependencies: Seq[ModuleID] =
    Seq(
      scalatest, testcontainers, airframe, pprint, configuration, scopt, betterFiles, monocle, avro4s, spark
    ).flatten

  lazy val dependenciesOverride: Seq[ModuleID] =
    Seq(
      jackson
    ).flatten
  
  lazy val scalatest: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % "test, it"
  )

  lazy val testcontainers: Seq[ModuleID] = Seq(
    "org.testcontainers" % "testcontainers" % "1.9.1" % "test, it"
  )

  lazy val airframe: Seq[ModuleID] = Seq(
    "org.wvlet.airframe" %% "airframe-log" % "0.69"
  )

  lazy val pprint: Seq[ModuleID] = Seq(
    "com.lihaoyi" %% "pprint" % "0.5.3" % "test, it"
  )

  lazy val configuration: Seq[ModuleID] = {
    val version = "0.9.2"

    Seq(
      "com.github.pureconfig" %% "pureconfig",
      "com.github.pureconfig" %% "pureconfig-http4s"
    ).map(_ % version)
  }
  
  lazy val scopt: Seq[ModuleID] = Seq(
    "com.github.scopt" %% "scopt" % "4.0.0-RC2"
  )

  lazy val betterFiles: Seq[ModuleID] = Seq(
    "com.github.pathikrit" %% "better-files" % "3.6.0"
  )
  
  lazy val monocle: Seq[ModuleID] = {
    val version = "1.5.0"

    Seq(
      "com.github.julien-truffaut" %% "monocle-law"
    ).map(_ % version % "test, it") ++ Seq(
      "com.github.julien-truffaut" %% "monocle-core",
      "com.github.julien-truffaut" %% "monocle-macro",
      "com.github.julien-truffaut" %% "monocle-generic"
    ).map(_ % version)
  }

  lazy val avro4s: Seq[ModuleID] = Seq(
    "com.sksamuel.avro4s" %% "avro4s-core" % "2.0.2"
  )

  lazy val jackson: Seq[ModuleID] = {
    val version = "2.8.8"

    Seq(
      "com.fasterxml.jackson.core" % "jackson-core",
      "com.fasterxml.jackson.core" % "jackson-databind",
      "com.fasterxml.jackson.module" %% "jackson-module-scala"
    ).map(_ % version force())
  }

  lazy val spark: Seq[ModuleID] = {
    val version = "2.4.3"

    Seq(
      "org.apache.spark" %% "spark-core",
      "org.apache.spark" %% "spark-sql",
      "org.apache.spark" %% "spark-streaming",
      "org.apache.spark" %% "spark-mllib",
      "org.apache.spark" %% "spark-hive",
      "org.apache.spark" %% "spark-graphx",
      "org.apache.spark" %% "spark-repl"
    ).map(_ % version)
  }
}