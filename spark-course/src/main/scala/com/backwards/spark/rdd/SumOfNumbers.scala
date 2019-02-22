package com.backwards.spark.rdd

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.SparkApp

/**
  * Create a Spark program to read the first 100 prime numbers from in/prime_nums.text
  * - print the sum of those numbers to console
  * Note each row of the input file contains 10 prime numbers separated by spaces
  */
object SumOfNumbers extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[*]")
  val sc: SparkContext = new SparkContext(conf)

  val lines: RDD[String] = sc textFile "spark-resources/in/prime-nums.txt"
  val nums: RDD[Int] = lines.flatMap(_.split("""\s+""")).filter(_.nonEmpty).map(_.toInt)

  println(s"Sum of first 100 primes: ${nums.take(100).sum}")
}