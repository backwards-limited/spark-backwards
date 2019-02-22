package com.backwards.spark.rdd.pair

import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.SparkApp

object PairRddFromRegularRdd extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[1]")
  val sc: SparkContext = new SparkContext(conf)

  val inputStrings = List("Lily 23", "Jack 29", "Mary 29", "James 8")
  val regularRDDs = sc parallelize inputStrings

  val split: String => (String, String) = { s =>
    val pattern = """(.*?)\s+(.*?)""".r

    val pattern(key, value) = s
    key -> value
  }

  val pairRDD = regularRDDs map split

  pairRDD.coalesce(1).saveAsTextFile("spark-resources/out/pair-rdd-from-regular-rdd")
}