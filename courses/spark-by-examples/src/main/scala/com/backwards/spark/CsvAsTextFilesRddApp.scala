package com.backwards.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object CsvAsTextFilesRddApp extends App {
  val spark: SparkSession = SparkSession.builder()
    .master("local[1]")
    .appName("text-files")
    .getOrCreate()

  val rdd: RDD[String] =
    spark.sparkContext.textFile("./courses/spark-by-examples/src/main/resources/csv/*")

  val splitRdd = rdd.map(_.split(","))

  splitRdd.foreach { case Array(col1, col2) =>
    println(s"Col1: $col1,  Col2: $col2")
  }

  println("--------------------------------")

  rdd.mapPartitionsWithIndex { (index, iterator) =>
    if (index == 0) iterator.drop(1)
    else iterator
  }.map(_.split(",")).foreach { case Array(col1, col2) =>
    println(s"Col1: $col1,  Col2: $col2")
  }
}