package com.backwards.spark

import cats.data.Kleisli
import cats.effect.IO
import cats.implicits._
import org.apache.spark.sql.functions.avg
import org.apache.spark.sql.streaming.StreamingQuery
import org.apache.spark.sql.types.{FloatType, StringType, StructType}
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import com.backwards.spark.Spark._
import better.files.Resource.{getUrl => resourceUrl}

/**
 * Run this app and manually copy csvs from "stock-market" resources directory to "incoming-stock-files"
 * {{{
 *   sbt "runMain com.backwards.spark._7a"
 * }}}
 */
object _7a {
  def main(args: Array[String]): Unit =
    program.unsafeRunSync()

  def program: IO[Unit] =
    sparkSession(_.appName("7a").master("local")).use { spark =>
      val program: Kleisli[IO, SparkSession, Unit] =
        for {
          csvs <- csvs
          aggs <- Kleisli.liftF(IO(csvs.groupBy("date").agg(avg(csvs.col("value")))))
          query <- query(aggs)
        } yield
          query.awaitTermination()

      program run spark
    }

  def csvs: Kleisli[IO, SparkSession, Dataset[Row]] =
    Kleisli { spark =>
      IO(new StructType().add("date", StringType).add("value", FloatType)).map(schema =>
        spark.readStream
          .option("sep", ",")
          .schema(schema)
          .csv(resourceUrl("incoming-stock-files").getFile)
      )
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
