package com.backwards.spark.rdd.pair

import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.SparkApp

object PairRddFromTupleList extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[1]")
  val sc: SparkContext = new SparkContext(conf)

  val tuples = List(("Lily", 23), ("Jack", 29), ("Mary", 29), ("James", 8))
  val pairRDD = sc parallelize tuples

  pairRDD coalesce 1 saveAsTextFile "spark-resources/out/pair-rdd-from-tuple-list"
}