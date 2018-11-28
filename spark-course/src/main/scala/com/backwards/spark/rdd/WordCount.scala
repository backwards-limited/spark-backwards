package com.backwards.spark.rdd

import scala.language.postfixOps
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.SparkApp

object WordCount extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[2]")
  val sc: SparkContext = new SparkContext(conf)

  val lines: RDD[String] = sc textFile "spark-resources/in/word-count.txt"
  val words: RDD[String] = lines.flatMap(_.split(" "))

  val wordCounts: collection.Map[String, Long] = words.countByValue()

  wordCounts foreach { case (word, count) =>
    info(s"$word : $count")
  }
}