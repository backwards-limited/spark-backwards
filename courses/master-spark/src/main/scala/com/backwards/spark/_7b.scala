package com.backwards.spark

import cats.data.Kleisli
import cats.effect.IO
import cats.implicits._
import org.apache.spark.sql.streaming.StreamingQuery
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import com.backwards.spark.Spark._

/**
 * The following is better explained in streaming.md
 *
 * First boot docker-compose.yml
 *
 * Boot a Kafka producer:
 * {{{
 *  kafka-console-producer --broker-list kafka:9092 --topic test
 * }}}
 * Then boot this app:
 * {{{
 *   sbt "runMain com.backwards.spark._7b"
 * }}}
 */
object _7b {
  def main(args: Array[String]): Unit =
    program.unsafeRunSync()

  def program: IO[Unit] =
    sparkSession(_.appName("7b").master("local")).use { spark =>
      import spark.implicits._

      val program: Kleisli[IO, SparkSession, Unit] =
        for {
          messages <- messages
          words <- Kleisli.liftF(IO(messages.flatMap(_.split(" "))))
          query <- query(words.groupBy("value").count())
        } yield
          query.awaitTermination()

      program run spark
    }

  def messages: Kleisli[IO, SparkSession, Dataset[String]] =
    Kleisli { spark =>
      import spark.implicits._

      IO delay
        spark.readStream
          .format("kafka")
          .option("kafka.bootstrap.servers", "localhost:9092")
          .option("subscribe", "test")
          .load()
          .selectExpr("CAST(value AS STRING)")
          // .selectExpr("CAST key AS STRING", "CAST value AS STRING") For key value
          .as[String]
    }

  def query(aggs: Dataset[Row]): Kleisli[IO, SparkSession, StreamingQuery] =
    Kleisli.liftF[IO, SparkSession, StreamingQuery](
      IO delay
        aggs.writeStream
          .outputMode("complete")
          .format("console")
          .start
    )
}