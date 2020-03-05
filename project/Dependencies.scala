import sbt._

object Dependencies {
  def apply(): Seq[sbt.ModuleID] = Seq(
    scalatest, testcontainers, airframe, pprint, configuration, scopt, betterFiles, monocle, avro4s, spark
  ).flatten

  def overrides: Seq[ModuleID] = Seq(
    jackson
  ).flatten
  
  lazy val scalatest: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.1.1" % "test, it"
  )

  lazy val testcontainers: Seq[ModuleID] = Seq(
    "org.testcontainers" % "testcontainers" % "1.12.5" % "test, it"
  )

  lazy val airframe: Seq[ModuleID] = Seq(
    "org.wvlet.airframe" %% "airframe-log" % "20.2.1"
  )

  lazy val pprint: Seq[ModuleID] = Seq(
    "com.lihaoyi" %% "pprint" % "0.5.9" % "test, it"
  )

  lazy val configuration: Seq[ModuleID] = {
    val group = "com.github.pureconfig"
    val version = "0.12.2"

    Seq(
      "pureconfig",
      "pureconfig-http4s"
    ).map(group %% _ % version)
  }
  
  lazy val scopt: Seq[ModuleID] = Seq(
    "com.github.scopt" %% "scopt" % "4.0.0-RC2"
  )

  lazy val betterFiles: Seq[ModuleID] = Seq(
    "com.github.pathikrit" %% "better-files" % "3.8.0"
  )
  
  lazy val monocle: Seq[ModuleID] = {
    val group = "com.github.julien-truffaut"
    val version = "2.0.2"

    Seq(
      "monocle-law"
    ).map(group %% _ % version % "test, it") ++ Seq(
      "monocle-core",
      "monocle-macro",
      "monocle-generic"
    ).map(group %% _ % version)
  }

  lazy val avro4s: Seq[ModuleID] = Seq(
    "com.sksamuel.avro4s" %% "avro4s-core" % "2.0.2"
  )

  lazy val jackson: Seq[ModuleID] = {
    val group = "com.fasterxml.jackson.core"
    val version = "2.8.8"

    Seq(
      "jackson-core",
      "jackson-databind",
      "jackson-module-scala"
    ).map(group %% _ % version force())
  }

  lazy val spark: Seq[ModuleID] = {
    val group = "org.apache.spark"
    val version = "2.4.5"

    Seq(
      "spark-core",
      "spark-sql",
      "spark-streaming",
      "spark-mllib",
      "spark-hive",
      "spark-graphx",
      "spark-repl"
    ).map(group %% _ % version)
  }
}