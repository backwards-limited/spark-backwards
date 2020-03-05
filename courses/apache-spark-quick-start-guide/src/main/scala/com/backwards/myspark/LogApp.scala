package com.backwards.myspark

import better.files.File
import org.apache.spark.sql.SparkSession

/**
 * Reads a log file and returns only the number of messages with log levels of ERROR and INFO
 */
object LogApp extends App {
  val spark: SparkSession = SparkSession
    .builder
    .appName("test")
    .config("spark.master", "local")
    .getOrCreate()

  val filePath = File("./data/input/sample.log").canonicalPath

  val logRDD = spark.sparkContext.textFile(filePath)

  val resultRDD = logRDD
    .filter(line => Array("INFO", "ERROR").contains(line.split(" -")(2).trim))
    .map(line => (line.split(" - ")(2), 1))
    .reduceByKey(_ + _)

  println(resultRDD.collect().toList)
}