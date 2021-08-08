import sbt._

object Dependencies {
  def apply(): Seq[ModuleID] = Seq(
    scalatest, testcontainers, airframe,
    pprint, pureConfig, scopt, decline,
    cats, catsEffect, catsEffectTesting, console4Cats, log4Cats,
    monocle, /*monix,*/ shapeless, chimney,
    circe, avro4s,
    sttp,
    betterFiles, spark, daria, hadoop, postgresql, awsJava
  ).flatten

  def overrides: Seq[ModuleID] = Seq(
    jackson
  ).flatten

  lazy val scalatest: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.2.7" % "test, it"
  )

  lazy val testcontainers: Seq[ModuleID] = {
    val group = "com.dimafeng"
    val version = "0.39.3"

    Seq(
      "testcontainers-scala-scalatest",
      "testcontainers-scala-kafka",
      "testcontainers-scala-cassandra",
      "testcontainers-scala-postgresql",
      "testcontainers-scala-localstack",
      "testcontainers-scala-mockserver"
    ).map(group %% _ % version % "test, it")
  }

  // TODO - Get rid of
  lazy val airframe: Seq[ModuleID] = Seq(
    "org.wvlet.airframe" %% "airframe-log" % "21.4.1"
  )

  lazy val pprint: Seq[ModuleID] = Seq(
    "com.lihaoyi" %% "pprint" % "0.6.4" % "test, it"
  )

  lazy val pureConfig: Seq[ModuleID] = {
    val group = "com.github.pureconfig"
    val version = "0.15.0"

    Seq(
      "pureconfig",
      "pureconfig-http4s"
    ).map(group %% _ % version)
  }
  
  lazy val scopt: Seq[ModuleID] = Seq(
    "com.github.scopt" %% "scopt" % "4.0.1"
  )

  lazy val decline: Seq[ModuleID] = Seq(
    "com.monovore" %% "decline" % "2.0.0"
  )

  lazy val cats: Seq[ModuleID] = {
    val group = "org.typelevel"
    val version = "2.6.0"

    Seq(
      "cats-core"
    ).map(group %% _ % version) ++ Seq(
      "cats-laws", "cats-testkit"
    ).map(group %% _ % version % "test, it")
  }

  lazy val catsEffect: Seq[ModuleID] = {
    val group = "org.typelevel"
    val version = "3.2.2"

    Seq(
      "cats-effect"
    ).map(group %% _ % version % "test, it")
  }

  lazy val catsEffectTesting: Seq[ModuleID] = Seq(
    "com.codecommit" %% "cats-effect-testing-scalatest" % "1.0-26-0b34520" % "test, it"
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

  lazy val monix: Seq[ModuleID] = Seq(
    "io.monix" %% "monix" % "3.3.0" withSources() withJavadoc()
  )

  lazy val shapeless: Seq[ModuleID] = Seq(
    "com.chuusai" %% "shapeless" % "2.3.4"
  )

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
    "com.sksamuel.avro4s" %% "avro4s-core" % "4.0.7"
  )

  lazy val sttp: Seq[ModuleID] = {
    val group = "com.softwaremill.sttp.client3"
    val version = "3.3.0-RC3"

    Seq(
      "core", "cats", "monix", "fs2", "async-http-client-backend-cats", "okhttp-backend", "circe"
    ).map(group %% _ % version)
  }

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
    val version = "3.1.1"

    Seq(
      "spark-core",
      "spark-sql",
      "spark-streaming",
      "spark-sql-kafka-0-10",
      "spark-mllib",
      "spark-hive",
      "spark-graphx",
      "spark-repl"
    ).map(group %% _ % version % "provided, test, it")
  }

  lazy val daria: Seq[ModuleID] = Seq(
    "com.github.mrpowers" %% "spark-daria" % "1.0.0"
  )

  lazy val hadoop: Seq[ModuleID] = {
    val group = "org.apache.hadoop"
    val version = "3.3.0"

    Seq(
      "hadoop-common",
      "hadoop-client",
      "hadoop-aws"
    ).map(group % _ % version % "provided, test, it")
  }

  lazy val postgresql: Seq[ModuleID] = Seq(
    "org.postgresql" % "postgresql" % "42.2.19"
  )

  lazy val awsJava: Seq[ModuleID] = Seq(
    "com.amazonaws" % "aws-java-sdk" % "1.11.1001"
  )
}

/*
// used for base64 encoding
libraryDependencies += "commons-codec" % "commons-codec" % "1.10"
*/