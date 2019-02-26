package com.backwards.spark.rdd.pair

import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.SparkApp

object GroupByKeyVsReduceByKey extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[3]")
  val sc: SparkContext = new SparkContext(conf)

  val words = List("one", "two", "two", "three", "three", "three")
  val wordsPairRdd = sc.parallelize(words).map(word => (word, 1))

  val wordCountsWithReduceByKey = wordsPairRdd.reduceByKey((x, y) => x + y).collect()
  println("wordCountsWithReduceByKey: " + wordCountsWithReduceByKey.toList)

  val wordCountsWithGroupByKey = wordsPairRdd.groupByKey().mapValues(intIterable => intIterable.size).collect()
  println("wordCountsWithGroupByKey: " + wordCountsWithGroupByKey.toList)
}