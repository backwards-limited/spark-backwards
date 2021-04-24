package com.backwards.spark

import java.net.URL
import better.files.Resource.{getUrl => resourceUrl}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, Row, SaveMode, SparkSession}
import com.backwards.spark.MapOps._
import com.backwards.spark.Spark._

object _1 {
  def main(args: Array[String]): Unit =
    program.unsafeRunSync()

  def program: IO[Unit] =
    sparkSession(_.appName("1").master("local")).use { implicit sparkSession =>
      loadResource(resourceUrl("name-and-comments.txt")) >>= commentsHaveNumber >>= withFullName >>= orderByLastName >>= show >>= persist
    }

  def loadResource(url: URL)(implicit sparkSession: SparkSession): IO[Dataset[Row]] =
    IO(sparkSession.read.format("csv").option("header", value = true).load(url.getFile))

  def commentsHaveNumber(ds: Dataset[Row]): IO[Dataset[Row]] =
    IO(ds.filter(ds.col("comment").rlike("""\d+""")))

  def withFullName(ds: Dataset[Row]): IO[Dataset[Row]] =
    IO(ds.withColumn("full_name", concat(ds.col("last_name"), lit(", "), ds.col("first_name"))))

  def orderByLastName(ds: Dataset[Row]): IO[Dataset[Row]] =
    IO(ds.orderBy(ds.col("last_name").asc))

  def show(ds: Dataset[Row]): IO[Dataset[Row]] =
    IO(ds.show()) *> IO(ds)

  def persist(ds: Dataset[Row]): IO[Unit] = IO {
    ds.write
      .mode(SaveMode.Overwrite)
      .jdbc(
        "jdbc:postgresql://localhost/world",
        "project1",
        Map(
          "driver" -> "org.postgresql.Driver",
          "user" -> "jimmy",
          "password" -> "banana"
        )
      )
  }
}
