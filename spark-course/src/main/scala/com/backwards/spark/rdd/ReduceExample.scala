package com.backwards.spark.rdd

import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.SparkApp

object ReduceExample extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[*]")
  val sc: SparkContext = new SparkContext(conf)

  val inputIntegers = List(1, 2, 3, 4, 5)
  val integerRdd = sc parallelize inputIntegers

  val product = integerRdd.reduce((x, y) => x * y)
  println(s"Product is: $product")
}