package com.backwards.spark

import scala.util.chaining.scalaUtilChainingOps
import cats.data.Kleisli
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import com.backwards.spark.Spark._

/**
 * Defaulting to running on AWS. Running locally:
 * {{{
 *  sbt '; project master-spark; set javaOptions ++= Seq("-Dmaster=local", "-Dfile=/Users/davidainslie/workspace/backwards/spark-backwards/courses/master-spark/src/main/resources/reddit-2007-small.json"); run'
 * }}}
 */
object _6 {
  val master: SparkSession.Builder => SparkSession.Builder =
    sb => Option(System.getProperty("master")).fold(sb)(m => sb.master(m))

  val file: String =
    Option(System.getProperty("file")) getOrElse "s3a://david-ainslie/reddit-2007-small.json"

  def main(args: Array[String]): Unit =
    program.unsafeRunSync()

  def program: IO[Unit] =
    sparkSession(_.appName("6").pipe(master)).use { spark =>
      import spark.implicits._

      val program: Kleisli[IO, SparkSession, Unit] = {
        for {
          words <- load(file).map(
            _.flatMap(_.mkString(" ").replaceAll("\n", "").replaceAll("\n", "").trim.toLowerCase.split(" ").iterator).toDF()
          )
          boringWords <- boringWords
        } yield
          words
            .join(boringWords, words.col("value").equalTo(boringWords.col("value")), "leftanti")
            .groupBy("value")
            .count()
            .orderBy(desc("count"))
            .show(20)
      }

      program run spark
    }

  def load(path: String): Kleisli[IO, SparkSession, Dataset[Row]] =
    Kleisli(spark => IO delay
      spark.read.format("json")
        .option("inferschema", value = "true")
        .option("header", value = true)
        .load(path)
    )

  def boringWords: Kleisli[IO, SparkSession, Dataset[Row]] =
    Kleisli { spark =>
      import spark.implicits._

      IO delay
        spark.createDataset(stopWords).toDF()
    }
}
