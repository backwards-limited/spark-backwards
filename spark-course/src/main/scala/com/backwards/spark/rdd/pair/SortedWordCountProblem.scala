package com.backwards.spark.rdd.pair

import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.SparkApp

/**
  * Create a Spark program to read the an article from in/word-count.txt,
  * output the number of occurrence of each word in descending order.
  *
  * Sample output:
  *
  * apple : 200
  * shoes : 193
  * bag : 176
  * ...
  */
object SortedWordCountProblem extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[3]")
  val sc: SparkContext = new SparkContext(conf)

  val lines = sc textFile "spark-resources/in/word-count.txt"

  val result = lines.flatMap(_.split("""\s+?""")).map(_ -> 1).reduceByKey(_ + _).map(flip).sortByKey(ascending = false).map(flip)

  result.collect().foreach { case (word, count) =>
    println(s"$word: $count")
  }

  def flip[K, V](kv: (K, V)): (V, K) = kv._2 -> kv._1
}