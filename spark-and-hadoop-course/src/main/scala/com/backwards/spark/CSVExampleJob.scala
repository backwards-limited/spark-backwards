package com.backwards.spark

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object CSVExampleJob extends App {
  val spark = SparkSession.builder
    .appName("csv-scala-spark")
    .getOrCreate()

  val filePath = args(0)

  val data = spark.read
    .option("header", value = true)
    .option("inferSchema", value = true)
    .option("timestampFormat", "dd/MM/yyyy")
    .csv(filePath)

  data.printSchema
  println(s"Count: ${data.count}")
  data.show

  val orderedData = data.orderBy(desc("Date of Payment"))
  orderedData.show(5)

  spark.stop()
}