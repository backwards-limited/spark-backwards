package com.backwards.spark.rdd

import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.SparkApp

object CountExample extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[*]")
  val sc: SparkContext = new SparkContext(conf)

  val inputWords = List("spark", "hadoop", "spark", "hive", "pig", "cassandra", "hadoop")
  val wordRdd = sc parallelize inputWords
  println(s"\nCount: ${wordRdd.count()}")

  val wordCountByValue = wordRdd.countByValue()

  println("\nCountByValue:")
  wordCountByValue.foreach { case (word, count) => println(s"$word: $count") }
}