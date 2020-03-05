package com.backwards.myspark

import better.files.File
import org.apache.spark.sql.SparkSession
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RddSpec extends AnyWordSpec with Matchers {
  val spark: SparkSession = SparkSession
    .builder
    .appName("test")
    .config("spark.master", "local")
    .getOrCreate()

  "RDD" should {
    "be created from parallize a collection" in {
      val numbersRDD = spark.sparkContext.parallelize(1 to 10)
      println(numbersRDD.collect().toList)
    }

    "be create from an external dataset" in {
      // NOTE As this module is part of the top level multi-module project, files are relative to that "root" when running
      val filePath = File("./data/input/some-employees.txt").canonicalPath
      val rdd = spark.sparkContext.textFile(filePath)
      println(rdd.collect().toList)
    }

    "be created from another RDD" in {
      val numbersRDD = spark.sparkContext.parallelize(1 to 10)

      val oddNumbersRDD = numbersRDD.filter(_ % 2 != 0)

      println(oddNumbersRDD.collect().toList)
    }

    "be created from a DataFrame or DataSet" in {
      val rangeDataFrame = spark.range(1, 5)
      val rangeRDD = rangeDataFrame.rdd

      println(rangeRDD.collect().toList)
    }
  }
}