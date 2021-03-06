package com.backwards.spark

import org.apache.spark.sql.SparkSession

object SimpleExampleJob extends App {
  val SPARK_HOME = sys env "SPARK_HOME"
  val logFile = s"$SPARK_HOME/README.md"

  val spark = SparkSession.builder
    .appName("first-scala-spark")
    .getOrCreate()

  val logData = spark.read.textFile(logFile).cache()

  val numAs = logData.filter(_.contains("a")).count()
  val numBs = logData.filter(_.contains("b")).count()

  println(s"Lines with a: $numAs, Lines with b: $numBs")

  spark.stop()
}