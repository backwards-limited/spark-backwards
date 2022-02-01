package com.backwards.spark

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import org.apache.spark.sql.streaming.StreamingQuery
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import com.backwards.spark.Spark._

/**
 * Before executing - start a socket connection at 9999 using:
 * {{{
 *  nc -lk 9999
 * }}}
 *
 * Then run:
 * {{{
 *   sbt "runMain com.backwards.spark._7"
 * }}}
 */
object _7 {
  def main(args: Array[String]): Unit =
    program.unsafeRunSync()

  def program: IO[Unit] =
    sparkSession(_.appName("7").master("local")).use { spark =>
      val program: Kleisli[IO, SparkSession, Unit] =
        for {
          words <- words
          wordCounts <- Kleisli.liftF(IO(words.groupBy("value").count()))
          query <- query(wordCounts)
        } yield
          query.awaitTermination()

      program run spark
    }

  def words: Kleisli[IO, SparkSession, Dataset[String]] =
    Kleisli { spark =>
      import spark.implicits._

      IO delay
        spark.readStream
          .format("socket")
          .option("host", "localhost")
          .option("port", 9999)
          .load()
          .as[String]
          .flatMap(_.split(" ").iterator)
    }

  def query(wordCounts: Dataset[Row]): Kleisli[IO, SparkSession, StreamingQuery] =
    Kleisli.liftF[IO, SparkSession, StreamingQuery](
      IO delay
        wordCounts.writeStream
          .outputMode("complete")
          .format("console")
          .start()
    )
}
