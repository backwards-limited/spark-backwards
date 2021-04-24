package com.backwards.spark

import java.net.URL
import better.files.Resource.{getUrl => resourceUrl}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import org.apache.spark.sql.types.DataTypes._
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.sql.{DataFrameReader, Dataset, Row, SparkSession}
import com.backwards.spark.Spark._

object _2 {
  lazy val schema: StructType =
    StructType(
      List(
        StructField("id", IntegerType, nullable = false),
        StructField("productId", IntegerType, nullable = true),
        StructField("title", StringType, nullable = false),
        StructField("publishDate", DateType, nullable = true),
        StructField("url", StringType, nullable = false)
      )
    )

  def main(args: Array[String]): Unit =
    program.unsafeRunSync()

  def program: IO[Unit] =
    sparkSession(_.appName("2").master("local")).use(implicit sparkSession =>
      (loadCsv(None) >>= show(7, 70) >>= printSchema) >>
      (loadCsv(Some(schema)) >>= show(7, 70) >>= printSchema) >>
      (loadJson(resourceUrl("simple.json"), multiline = false) >>= show(7, 70) >>= printSchema) >>
      (loadJson(resourceUrl("multiline.json"), multiline = true) >>= show(7, 70) >>= printSchema)
    )

  /**
   * Example from CSV:
   * {{{
   *  listingId;productId;title;publishDate;url
   *  1;12;^TP-Link AC750 Dual Band WiFi Range Extender, Repeater, Access Point w/Mini Housing
   *  Design, Extends WiFi to Smart Home & Alexa Devices (RE200)^;09/10/17;http://a.co/d/3ivKXxI
   * }}}
   */
  def loadCsv(schema: Option[StructType])(implicit sparkSession: SparkSession): IO[Dataset[Row]] = {
    val schemaOrInferred: DataFrameReader => DataFrameReader =
      dfr => schema.fold(dfr.option("inferSchema", value = true))(dfr.schema)

    IO {
      schemaOrInferred(
        sparkSession.read.format("csv")
          .option("header", value = true)
          .option("multiline", value = true)
          .option("sep", value = ";")
          .option("quote", value = "^")
          .option("dateFormat", value = "M/d/y")
      ).load(resourceUrl("amazon-products.txt").getFile)
    }
  }

  def loadJson(url: URL, multiline: Boolean)(implicit sparkSession: SparkSession): IO[Dataset[Row]] =
    IO(sparkSession.read.format("json").option("multiline", multiline).load(url.getFile))

  def show(numberOfRows: Int, maxCharactersInColumn: Int)(ds: Dataset[Row]): IO[Dataset[Row]] =
    IO(ds.show(numberOfRows, maxCharactersInColumn)) *> IO(ds)

  def printSchema(ds: Dataset[Row]): IO[Unit] =
    IO(ds.printSchema)
}
