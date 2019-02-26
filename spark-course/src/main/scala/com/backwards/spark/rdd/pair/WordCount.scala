package com.backwards.spark.rdd.pair

import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.SparkApp

object WordCount extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[3]")
  val sc: SparkContext = new SparkContext(conf)

  val lines = sc textFile "spark-resources/in/word-count.txt"
  val wordRdd = lines.flatMap(_.split(" "))
  val wordPairRdd = wordRdd.map(word => (word, 1))

  val wordCounts = wordPairRdd.reduceByKey(_ + _)

  wordCounts.collect().foreach { case (word, count) => println(s"$word: $count")}
}