package com.backwards.spark

import org.apache.spark._

/**
  * A very short job for stress tests purpose.
  * Small data. Double every value in the data.
  */
object SimpleExampleJob extends App {
  val conf = new SparkConf().setMaster("local[3]").setAppName("SimpleExampleJob")
  val sc = new SparkContext(conf)

  try {
    val data = Array(1, 2, 3)


    val dd = sc.parallelize(data)

    val results = dd.map(_ * 2).collect()
    println(s"Result is: ${results.mkString(", ")}")
  } finally {
    sc.stop()
  }
}