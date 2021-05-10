package com.backwards.spark.ch3

import better.files._
import cats.data.ReaderT
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import com.backwards.spark.Spark.sparkSession
import cats.implicits._

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
 */
object IngestionSchemaManipulationScalaApp {
  def main(array: Array[String]): Unit =
    sparkSession(_.appName("ingestion-schema-manipulation").master("local[*]"))
      .use(program.run)
      .unsafeRunSync()

  def program: ReaderT[IO, SparkSession, Dataset[Row]] =
    ReaderT(readEmployees)

  println(Resource.getUrl("ch3/restaurants-in-wake-county.csv").getFile)

  def readEmployees(spark: SparkSession): IO[Dataset[Row]] =
    IO(spark.read.format("csv").option("header", "true").load(Resource.getUrl("ch3/restaurants-in-wake-county.csv").getFile))
      .flatTap(ds => IO(ds.show(5)))
}