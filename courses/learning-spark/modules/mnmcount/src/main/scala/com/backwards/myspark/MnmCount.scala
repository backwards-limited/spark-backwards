package com.backwards.myspark

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

/**
 * Usage: MnmCount <mnm_file_dataset>
 */
object MnmCount {
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("MnmCount")
      .getOrCreate()

    if (args.length < 1) {
      print("Usage: MnmCount <mnm_file_dataset>")
      sys.exit(1)
    }

    // Get the M&M data set file name
    val mnm_file = args(0)

    // Read the file into a Spark DataFrame
    val mnm_df = spark.read.format("csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load(mnm_file)

    // Aggregate count of all colors and groupBy state and color orderBy descending order
    val count_mnm_df = mnm_df.select("State", "Color", "Count")
      .groupBy("State", "Color")
      .agg(count("Count").alias("Total"))
      .orderBy(desc("Total"))

    // Show all the resulting aggregation for all the dates and colors
    count_mnm_df.show(60)
    println("Total Rows = %d", count_mnm_df.count())
    println()

    // Find the aggregate count for California by filtering
    val ca_count_mnm_df = mnm_df.select("*")
      .where(col("State") === "CA")
      .groupBy("State", "Color")
      .agg(count("Count").alias("Total"))
      .orderBy(desc("Total"))

    // Show the resulting aggregation for California
    ca_count_mnm_df.show(10)

    // Stop the SparkSession
    spark.stop()
  }
}