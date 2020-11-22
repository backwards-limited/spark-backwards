package com.backwards.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object WholeTextFilesRddApp extends App {
  val spark: SparkSession = SparkSession.builder()
    .master("local[1]")
    .appName("text-files")
    .getOrCreate()

  val rdd: RDD[(String, String)] =
    spark.sparkContext.wholeTextFiles("./courses/spark-by-examples/src/main/resources/txt/*")

  rdd.foreach { case (path, content) =>
    println(s"$path")
    println(s"$content")
  }
}