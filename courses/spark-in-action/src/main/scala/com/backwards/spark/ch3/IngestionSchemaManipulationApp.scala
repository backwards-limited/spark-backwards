package com.backwards.spark.ch3

import scala.util.chaining.scalaUtilChainingOps
import better.files._
import cats.data.ReaderT
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import org.apache.spark.sql.functions.{col, concat, lit, split}
import org.apache.spark.sql.{Column, Dataset, Row, SparkSession}
import com.backwards.spark.Spark.sparkResource

/**
 * Read CSV which has:
 * {{{
 *  OBJECTID,               HSISID,       NAME,           ADDRESS1,     ADDRESS2,   CITY,     STATE,POSTALCODE,PHONENUMBER,RESTAURANTOPENDATE,FACILITYTYPE,PERMITID,X,Y,GEOCODESTATUS
 *  1446808,                04092017187,  THE 19TH HOLE,  1527 TRYON RD,          , RALEIGH,  NC,27603,(919) 772-9987,2016/04/01 00:00:00+00,Restaurant,18,-78.66755526,35.73521897,M
 * }}}
 * which be transformed to:
 * {{{
 *  id (calculated field),  datasetId     name...
 * }}}
 * The goal is to merge 2 dataframes (just as you can perform a SQL union of 2 tables).
 * To make the union effective, we need similarly named columns in both dataframes.
 */
object IngestionSchemaManipulationApp {
  def main(array: Array[String]): Unit =
    sparkResource(_.appName("ingestion-schema-manipulation").master("local[*]"))
      .use(program.run)
      .unsafeRunSync()

  def program: ReaderT[IO, SparkSession, Dataset[Row]] =
    for {
      wakeRestaurantsDF <- wakeRestaurants
      durhamRestaurantsDF <- durhamRestaurants
    } yield
      wakeRestaurantsDF.unionByName(durhamRestaurantsDF).tap(_.show(5)).tap(_.printSchema()).tap(df => println(s"Partiton count: ${df.rdd.partitions.length}"))

  val wakeRestaurants: ReaderT[IO, SparkSession, Dataset[Row]] =
    ReaderT(spark =>
      IO(spark.read.format("csv").option("header", "true").load(Resource.getUrl("ch3/restaurants-in-wake-county.csv").getFile))
        .map(
          _.withColumn("county", lit("Wake"))
            .withColumnRenamed("HSISID", "datasetId")
            .withColumnRenamed("NAME", "name")
            .withColumnRenamed("ADDRESS1", "address1")
            .withColumnRenamed("ADDRESS2", "address2")
            .withColumnRenamed("CITY", "city")
            .withColumnRenamed("STATE", "state")
            .withColumnRenamed("POSTALCODE", "zip")
            .withColumnRenamed("PHONENUMBER", "tel")
            .withColumnRenamed("RESTAURANTOPENDATE", "dateStart")
            .withColumn("dateEnd", lit(null))
            .withColumnRenamed("FACILITYTYPE", "type")
            .withColumnRenamed("X", "geoX")
            .withColumnRenamed("Y", "geoY")
            .withColumn("id", concat(col("state"), lit("_"), col("county"), lit("_"), col("datasetId")))
            .drop("OBJECTID", "PERMITID", "GEOCODESTATUS")
        )
        .flatTap(df => IO(df.show(5)))
        .flatTap(df => IO(df.printSchema()))
        .flatMap { df =>
          IO {
            // Partitions are not directly accessible from the dataframe; you will need to look at partitions through the RDDs.
            println(s"Partition count before repartition: ${df.rdd.partitions.length}")
            df.repartition(4)
          }.flatTap(df => IO(println(s"Partition count after repartition: ${df.rdd.partitions.length}")))
        }
    )

  val durhamRestaurants: ReaderT[IO, SparkSession, Dataset[Row]] = {
    val idColumn: List[Column] =
      List(col("state"), lit("_"), col("county"), lit("_"), col("datasetId"))

    ReaderT(spark =>
      IO(spark.read.format("json").load(Resource.getUrl("ch3/restaurants-in-durham-county.json").getFile))
        .map(
          _.withColumn("county", lit("Durham"))
            .withColumn("datasetId", col("fields.id"))
            .withColumn("name", col("fields.premise_name"))
            .withColumn("address1", col("fields.premise_address1"))
            .withColumn("address2", col("fields.premise_address2"))
            .withColumn("city", col("fields.premise_city"))
            .withColumn("state", col("fields.premise_state"))
            .withColumn("zip", col("fields.premise_zip"))
            .withColumn("tel", col("fields.premise_phone"))
            .withColumn("dateStart", col("fields.opening_date"))
            .withColumn("dateEnd", col("fields.closing_date"))
            .withColumn("type", split(col("fields.type_description"), " - ").getItem(1))
            .withColumn("geoX", col("fields.geolocation").getItem(0))
            .withColumn("geoY", col("fields.geolocation").getItem(1))
            .withColumn("id", concat(idColumn: _*))
            .drop("fields", "geometry", "record_timestamp", "recordid")
        )
        .flatTap(df => IO(df.show(5)))
        .flatTap(df => IO(df.printSchema()))
        .flatMap { df =>
          IO {
            // Partitions are not directly accessible from the dataframe; you will need to look at partitions through the RDDs.
            println(s"Partition count before repartition: ${df.rdd.partitions.length}")
            df.repartition(4)
          }.flatTap(df => IO(println(s"Partition count after repartition: ${df.rdd.partitions.length}")))
        }
    )
  }
}