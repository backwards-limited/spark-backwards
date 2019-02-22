package com.backwards.spark.rdd

import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.SparkApp

object TakeExample extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[*]")
  val sc: SparkContext = new SparkContext(conf)

  val inputWords = List("spark", "hadoop", "spark", "hive", "pig", "cassandra", "hadoop")
  val wordRdd = sc parallelize inputWords

  val words = wordRdd take 3

  words foreach println
}