package com.backwards.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object TextFilesRddApp extends App {
  val spark: SparkSession = SparkSession.builder()
    .master("local[1]")
    .appName("text-files")
    .getOrCreate()

  val rdd: RDD[String] =
    spark.sparkContext.textFile("./courses/spark-by-examples/src/main/resources/txt/*")

  rdd.foreach(println)
}