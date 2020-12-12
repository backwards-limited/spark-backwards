package com.backwards.spark

import scala.util.chaining._
import cats.data.Kleisli
import cats.effect.IO
import cats.implicits._
import org.apache.spark.sql.{Dataset, SparkSession}
import com.backwards.spark.Spark._

object _4 {
  def main(args: Array[String]): Unit =
    program.unsafeRunSync()

  def program: IO[Unit] =
    sparkSession(_.appName("4").master("local")).use { spark =>
      import spark.implicits._

      val program: Kleisli[IO, SparkSession, Unit] =
        spark.createDataset(List("Banana", "Car", "Glass", "Banana", "Computer", "Car")).pipe { ds =>
          for {
            _ <- counting(ds)
            _ <- mapping(ds)
            _ <- reducing(ds)
          } yield ()
        }

      program.run(spark)
    }

  def counting(ds: Dataset[String]): Kleisli[IO, SparkSession, Unit] =
    Kleisli { spark =>
      import spark.implicits._

      IO(
        ds.tap(_.printSchema()).tap(_.show)
          .groupBy("value")
          .count()                    // Generates a DataFrame from our Dataset (to get a DataFrame directly do: ds.toDF)
          .tap(_.show())
          .pipe(_.as[(String, Long)]) // We are going back to a Dataset from the DataFrame which had a tuple
          .pipe(_.show())
      )
    }

  def mapping(ds: Dataset[String]): Kleisli[IO, SparkSession, Unit] =
    Kleisli { spark =>
      import spark.implicits._

      IO(ds.map("word: " + _).show())
    }

  def reducing(ds: Dataset[String]): Kleisli[IO, SparkSession, Unit] =
    Kleisli.liftF(IO(ds.reduce(_ + _))).map(println)
}
