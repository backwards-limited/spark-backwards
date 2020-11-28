package com.backwards.myspark

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

/**
 * Usage: MnMcount <mnm_file_dataset>
 */
object MnMcount {
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("MnMCount")
      .getOrCreate()

    if (args.length < 1) {
      print("Usage: MnMcount <mnm_file_dataset>")
      sys.exit(1)
    }

    spark.sparkContext.setLogLevel("ERROR")

    // Get the M&M data set file name
    val mnmFile = args(0)

    // Read the file into a Spark DataFrame
    val mnmDF = spark.read.format("csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load(mnmFile)

    // Display DataFrame
    mnmDF.show(5, truncate = false)

    // Aggregate count of all colors and groupBy state and color orderBy descending order
    val countMnMDF = mnmDF.select("State", "Color", "Count")
      .groupBy("State", "Color")
      .sum("Count")
      .orderBy(desc("sum(Count)"))

    // Show all the resulting aggregation for all the dates and colors
    countMnMDF.show(60)
    println(s"Total Rows = ${countMnMDF.count()}")
    println()

    // Find the aggregate count for California by filtering
    val caCountMnNDF = mnmDF.select("*")
      .where(col("State") === "CA")
      .groupBy("State", "Color")
      .sum("Count")
      .orderBy(desc("sum(Count)"))

    // Show the resulting aggregation for California
    caCountMnNDF.show(10)
  }
}