package com.backwards.spark

import java.net.URL
import scala.util.chaining._
import better.files.Resource.{getUrl => resourceUrl}
import cats.data.Kleisli
import cats.effect.IO
import cats.implicits._
import org.apache.spark.sql.{Dataset, SparkSession}
import com.backwards.spark.Spark._

object _4a {
  def main(args: Array[String]): Unit =
    program.unsafeRunSync()

  def program: IO[Unit] =
    sparkSession(_.appName("4a").master("local")).use { spark =>
      val program: Kleisli[IO, SparkSession, Unit] =
        load(resourceUrl("houses.csv"))
          .map(_.tap(_.printSchema()).pipe(_.show()))

      program.run(spark)
    }

  def load(url: URL): Kleisli[IO, SparkSession, Dataset[House]] =
    Kleisli { spark =>
      IO delay
        spark.read.format("csv")
          .option("inferschema", value = "true")
          .option("header", value = true)
          .option("sep", value = ";")
          .load(url.getFile)
          .as[House]
    }
}
