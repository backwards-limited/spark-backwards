import sbt._

object Dependencies {
  def apply(): Seq[sbt.ModuleID] = Seq(
    scalatest, testcontainers, airframe,
    pprint, pureConfig, scopt, decline,
    cats, catsEffectTesting, console4Cats, log4Cats,
    monocle, chimney,
    circe, avro4s,
    betterFiles,
    spark, hadoop, postgresql
  ).flatten

  def overrides: Seq[ModuleID] = Seq(
    jackson
  ).flatten

  lazy val scalatest: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.2.3" % "test, it"
  )

  lazy val testcontainers: Seq[ModuleID] = Seq(
    "org.testcontainers" % "testcontainers" % "1.15.0" % "test, it"
  )

  lazy val airframe: Seq[ModuleID] = Seq(
    "org.wvlet.airframe" %% "airframe-log" % "20.11.0"
  )

  lazy val pprint: Seq[ModuleID] = Seq(
    "com.lihaoyi" %% "pprint" % "0.6.0" % "test, it"
  )

  lazy val pureConfig: Seq[ModuleID] = {
    val group = "com.github.pureconfig"
    val version = "0.14.0"

    Seq(
      "pureconfig",
      "pureconfig-http4s"
    ).map(group %% _ % version)
  }
  
  lazy val scopt: Seq[ModuleID] = Seq(
    "com.github.scopt" %% "scopt" % "4.0.0-RC2"
  )

  lazy val decline: Seq[ModuleID] = Seq(
    "com.monovore" %% "decline" % "1.3.0"
  )

  lazy val cats: Seq[ModuleID] = {
    val group = "org.typelevel"
    val version = "2.2.0"

    Seq(
      "cats-core", "cats-effect"
    ).map(group %% _ % version) ++ Seq(
      "cats-laws", "cats-testkit"
    ).map(group %% _ % version % "test, it")
  }

  lazy val catsEffectTesting: Seq[ModuleID] = Seq(
    "com.codecommit" %% "cats-effect-testing-scalatest" % "0.5.0" % "test, it"
  )

  lazy val console4Cats: Seq[ModuleID] = Seq(
    "dev.profunktor" %% "console4cats" % "0.8.1"
  )

  lazy val log4Cats: Seq[ModuleID] = {
    val group = "io.chrisdavenport"
    val version = "1.1.1"

    Seq(
      "log4cats-core", "log4cats-slf4j"
    ).map(group %% _ % version)
  }

  lazy val monocle: Seq[ModuleID] = {
    val group = "com.github.julien-truffaut"
    val version = "2.1.0"

    Seq(
      "monocle-core", "monocle-macro", "monocle-generic"
    ).map(group %% _ % version) ++ Seq(
      "monocle-law"
    ).map(group %% _ % version % "test, it")
  }

  lazy val chimney: Seq[ModuleID] = Seq(
    "io.scalaland" %% "chimney" % "0.6.1"
  )

  lazy val circe: Seq[ModuleID] = {
    val group = "io.circe"
    val version = "0.13.0"

    Seq(
      "circe-core", "circe-generic", "circe-generic-extras", "circe-parser", "circe-refined", "circe-optics", "circe-literal", "circe-jawn"
    ).map(group %% _ % version) ++ Seq(
      "circe-testing"
    ).map(group %% _ % version % "test, it")
  }

  lazy val avro4s: Seq[ModuleID] = Seq(
    "com.sksamuel.avro4s" %% "avro4s-core" % "4.0.2"
  )

  lazy val betterFiles: Seq[ModuleID] = Seq(
    "com.github.pathikrit" %% "better-files" % "3.9.1"
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
    val version = "3.0.1"

    Seq(
      "spark-core",
      "spark-sql",
      "spark-streaming",
      "spark-sql-kafka-0-10",
      "spark-mllib",
      "spark-hive",
      "spark-graphx",
      "spark-repl"
    ).map(group %% _ % version % Provided)
  }

  lazy val hadoop: Seq[ModuleID] = {
    val group = "org.apache.hadoop"
    val version = "3.3.0"

    Seq(
      "hadoop-common",
      "hadoop-client",
      "hadoop-aws"
    ).map(group % _ % version % Provided)
  }

  lazy val postgresql: Seq[ModuleID] = Seq(
    "org.postgresql" % "postgresql" % "42.2.18"
  )
}

/*
// used for base64 encoding
libraryDependencies += "commons-codec" % "commons-codec" % "1.10"
*/