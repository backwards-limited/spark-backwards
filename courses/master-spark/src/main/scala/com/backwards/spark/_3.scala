package com.backwards.spark

import scala.util.chaining._
import better.files.Resource.{getUrl => resourceUrl}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import com.backwards.spark.Spark._

object _3 {
  def main(args: Array[String]): Unit =
    program.unsafeRunSync()

  def program: IO[Unit] =
    sparkSession(_.appName("3").master("local")).use(implicit sparkSession =>
      (loadJson >>= show(7, 70) >>= printSchema, loadCsv >>= show(7, 70) >>= printSchema).mapN(_ unionByName _) >>= results
    )

  def results(ds: Dataset[Row]): IO[Unit] =
    IO(ds.tap(_.show(500)).tap(_.printSchema()).pipe(_.repartition(5))) >>=
      (ds => IO(println(s"Total number of partitions = ${ds.rdd.partitions.length}, and total records = ${ds.count()}")))

  def loadJson(implicit sparkSession: SparkSession): IO[Dataset[Row]] =
    IO(
      sparkSession.read.format("json")
        .option("multiline", value = true)
        .load(resourceUrl("durham-parks.json").getFile)
    ).map(ds =>
      ds.withColumn("park_id", concat(ds.col("datasetid"), lit("_"), ds.col("fields.objectid"), lit("_Durham")))
        .withColumn("park_name", ds.col("fields.park_name"))
        .withColumn("city", lit("Durham"))
        .withColumn("address", ds.col("fields.address"))
        .withColumn("has_playground", ds.col("fields.playground"))
        .withColumn("zipcode", ds.col("fields.zip"))
        .withColumn("land_in_acres", ds.col("fields.acres"))
        .withColumn("geoX", ds.col("geometry.coordinates").getItem(0))
        .withColumn("geoY", ds.col("geometry.coordinates").getItem(1))
        .drop("fields").drop("geometry").drop("record_timestamp").drop("recordid").drop("datasetid")
    )

  def loadCsv(implicit sparkSession: SparkSession): IO[Dataset[Row]] =
    IO(
      sparkSession.read.format("csv")
        .option("header", value = true)
        .option("multiline", value = true)
        .load(resourceUrl("philadelphia-recreations.csv").getFile)
    ).map(ds =>
      ds.filter(lower(ds.col("USE_")).like("%park%"))
      // OR: ds.filter("lower(USE_) like '%park%'")
    ).map(ds =>
      ds.withColumn("park_id", concat(lit("phil_"), ds.col("OBJECTID")))
        .withColumnRenamed("ASSET_NAME", "park_name")
        .withColumn("city", lit("Philadelphia"))
        .withColumnRenamed("ADDRESS", "address")
        .withColumn("has_playground", lit("UNKNOWN"))
        .withColumnRenamed("ZIPCODE", "zipcode")
        .withColumnRenamed("ACREAGE", "land_in_acres")
        .withColumn("geoX", lit("UNKNONW"))
        .withColumn("geoY", lit("UNKNONW"))
        .drop("SITE_NAME")
        .drop("OBJECTID")
        .drop("CHILD_OF")
        .drop("TYPE")
        .drop("USE_")
        .drop("DESCRIPTION")
        .drop("SQ_FEET")
        .drop("ALLIAS")
        .drop("CHRONOLOGY")
        .drop("NOTES")
        .drop("DATE_EDITED")
        .drop("EDITED_BY")
        .drop("OCCUPANT")
        .drop("TENANT")
        .drop("LABEL")
    )

  def show(numberOfRows: Int, maxCharactersInColumn: Int)(ds: Dataset[Row]): IO[Dataset[Row]] =
    IO(ds.show(numberOfRows, maxCharactersInColumn)) *> IO(ds)

  def printSchema(ds: Dataset[Row]): IO[Dataset[Row]] =
    IO(ds.printSchema()) *> IO(ds)
}
