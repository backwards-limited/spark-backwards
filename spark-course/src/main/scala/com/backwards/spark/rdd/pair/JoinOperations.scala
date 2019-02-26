package com.backwards.spark.rdd.pair

import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.SparkApp

object JoinOperations extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[1]")
  val sc: SparkContext = new SparkContext(conf)

  val ages = sc parallelize List(("Tom", 29), ("John", 22))
  val addresses = sc parallelize List(("James", "USA"), ("John", "UK"))

  val join = ages join addresses
  join saveAsTextFile "spark-resources/out/age-address-join.txt"

  val leftOuterJoin = ages leftOuterJoin addresses
  leftOuterJoin saveAsTextFile "spark-resources/out/age-address-left-out-join.txt"

  val rightOuterJoin = ages rightOuterJoin addresses
  rightOuterJoin saveAsTextFile "spark-resources/out/age-address-right-out-join.txt"

  val fullOuterJoin = ages fullOuterJoin addresses
  fullOuterJoin saveAsTextFile "spark-resources/out/age-address-full-out-join.txt"
}