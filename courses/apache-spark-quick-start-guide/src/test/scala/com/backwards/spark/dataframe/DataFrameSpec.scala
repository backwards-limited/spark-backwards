package com.backwards.spark.dataframe

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DataFrameSpec extends AnyWordSpec with Matchers {
  val spark: SparkSession = SparkSession
    .builder
    .appName("DataFrame example")
    .config("spark.master", "local")
    .config("spark.some.config.option", "value")
    .getOrCreate

  // For implicit conversions like converting RDDs to DataFrames and SQL encoders (as needed below)
  import spark.implicits._

  val salesDataFrame: DataFrame = spark
    .read
    .option("sep", "\t")
    .option("header", "true")
    .csv("data/input/sample_10000.txt")

  salesDataFrame.printSchema

  salesDataFrame.select("firstname").show

  salesDataFrame.filter($"id" < 5).show

  salesDataFrame.groupBy("ip").count().show

  "DataFrame" should {
    "be created from file" in {
      salesDataFrame.show
    }

    "load parquet" in {
      salesDataFrame.write.parquet("target/sales.parquet")

      val parquetSalesDataFrame = spark
        .read
        .parquet("target/sales.parquet")

      parquetSalesDataFrame.createOrReplaceTempView("parquetSales")

      val ipDataFrame = spark
        .sql("SELECT ip FROM parquetSales WHERE id BETWEEN 10 AND 19")

      ipDataFrame.map(row => s"IP: ${row(0)}").show
    }

    "load JSON" in {
      salesDataFrame.write.json("target/sales.json")

      val jsonSalesDataFrame = spark.read.json("target/sales.json")

      jsonSalesDataFrame.createOrReplaceTempView("jsonSales")

      val ipDataFrame = spark
        .sql("SELECT ip FROM jsonSales WHERE id BETWEEN 10 AND 19")

      ipDataFrame.map(row => s"IP:${row(0)}").show
    }
  }
}