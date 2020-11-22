package com.backwards.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object RddParallelizeApp extends App {
  val spark: SparkSession = SparkSession.builder()
    .master("local[3]")
    .appName("SparkByExamples.com")
    .getOrCreate()

  val rdd: RDD[Int] = spark.sparkContext.parallelize(List(1, 2, 3, 4, 5))

  val rddCollect: Array[Int] = rdd.collect()

  println(s"Number of Partitions: ${rdd.getNumPartitions}")
  println(s"Action: First element: ${rdd.first()}")
  println("Action: RDD converted to Array[Int]: ")
  rddCollect foreach println
}