package com.backwards.sales

import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.functions._

object Sales {
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("Sales")
      // .config("spark.master", "local")
      .config("spark.scheduler.mode", "FAIR")
      .getOrCreate

    // For implicit conversions like converting RDDs to DataFrames and SQL encoders (as needed below)
    import spark.implicits._

    val salesDataFrame: DataFrame = spark
      .read
      .option("inferSchema", "true")
      .option("header", "true")
      .csv("hdfs:///tmp/sales.csv")

    val dataset: Dataset[Row] = salesDataFrame
      .groupBy("COUNTRY_CODE")
      .sum("AMOUNT")
      .orderBy(desc("sum(AMOUNT)"))

    dataset.show

    // Stop the SparkSession
    spark.stop()
  }
}