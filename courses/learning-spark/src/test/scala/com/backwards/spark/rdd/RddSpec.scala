package com.backwards.spark.rdd

import better.files.Resource
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, SparkSession}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

/**
 * [[https://www.edureka.co/blog/rdd-using-spark/ RDD using Spark]]
 */
class RddSpec extends AnyWordSpec with Matchers {
  val spark: SparkSession =
    SparkSession.builder.appName("rdd").config("spark.master", "local").getOrCreate

  import spark.implicits._

  "Rdd creation" should {
    "come from parallelized collections" in {
      val PCRDD: RDD[String] = spark.sparkContext.parallelize(List("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"), 2)

      val resultRDD: Array[String] = PCRDD.collect()
      resultRDD.foreach(println)
    }

    "come by applying transformation on previous RDD" in {
      val words: RDD[String] = spark.sparkContext.parallelize(Seq("Spark", "is", "a", "very", "powerful", "language", "lalala"))

      val wordpair: RDD[(Char, String)] = words.map(w => w.charAt(0) -> w)
      wordpair.collect().foreach(println)
    }

    "come from reading data from external storage of file paths" in {
      val path: String = Resource.getUrl("log4j.properties").getFile

      val sparkfile: RDD[String] = spark.sparkContext.textFile(path)
      sparkfile.collect().foreach(println)
    }
  }

  /**
   * Transformations: The operations we apply on RDDs to filter, access and modify the data in parent RDD to generate a successive RDD is called transformation.
   * The new RDD returns a pointer to the previous RDD ensuring the dependency between them.
   * Transformations are Lazy Evaluations, in other words, the operations applied on the RDD that you are working will be logged but not executed.
   *
   * Narrow Transformations: We apply narrow transformations on to a single partition of the parent RDD to generate a new RDD e.g.
   *
   *  - map()
   *  - filter()
   *  - flatMap()
   *  - partition()
   *  - mapPartitions()
   *
   * Wide Transformations: We apply the wide transformation on multiple partitions to generate a new RDD e.g.
   *
   *  - reduceBy()
   *  - union()
   */
  "Rdd transformations" should {
    "filter" in {
      val path: String = Resource.getUrl("matches.csv").getFile

      val sparkfile: RDD[String] = spark.sparkContext.textFile(path)

      val matches: RDD[String] = sparkfile.filter(_ contains "Jaipur")

      println("\n===> FILTER")
      matches.collect.foreach(println)
    }

    "flatMap" in {
      val path: String = Resource.getUrl("matches.csv").getFile

      val sparkfile: RDD[String] = spark.sparkContext.textFile(path)

      val matches: RDD[String] = sparkfile.filter(_ contains "Jaipur")

      println("\n===> FLATMAP")
      matches.flatMap(_.split("Jaipur")).collect.foreach(println)
    }

    "mapPartitions" in {
      val path: String = Resource.getUrl("matches.csv").getFile

      val sparkfile: RDD[String] = spark.sparkContext.textFile(path)

      val matches: RDD[String] = sparkfile.filter(_ contains "Jaipur")

      println("\n===> MAPPARTITIONS")
      matches.mapPartitions(iterOfString => Iterator(iterOfString.length)).collect.foreach(println)
    }

    "reduceByKey" in {
      val path: String = Resource.getUrl("matches.csv").getFile

      val sparkfile: RDD[String] = spark.sparkContext.textFile(path)

      val matches: RDD[String] = sparkfile.map(_.split(",")(14))
      val manOfMatchOne: RDD[(String, Int)] = matches.map(_ -> 1)
      val manOfMatchCount: RDD[(Int, String)] = manOfMatchOne.reduceByKey(_ + _).map { case (key, count) => count -> key }.sortByKey(ascending = false)

      println("\n===> REDUCEBYKEY - Man of the match")
      manOfMatchCount.take(20).foreach(println)
    }

    "union" in {
      val path: String = Resource.getUrl("matches.csv").getFile

      val sparkfile: RDD[String] = spark.sparkContext.textFile(path)

      val jaipur: RDD[String] = sparkfile.filter(_ contains "Jaipur")
      val bangalore: RDD[String] = sparkfile.filter(_ contains "Bangalore")

      println("\n===> UNION")
      jaipur.union(bangalore).collect.foreach(println)
    }
  }

  /**
   * Actions: Instruct Apache Spark to apply computation and pass the result or an exception back to the driver RDD e.g.
   *
   *  - collect()
   *  - count()
   *  - take()
   *  - first()
   */
  "Rdd actions" should {
    "map and take (collect) i.e. transform and action" in {
      val path: String = Resource.getUrl("matches.csv").getFile

      val sparkfile: RDD[String] = spark.sparkContext.textFile(path)
      sparkfile.take(20).foreach(println)

      val cities: RDD[String] = sparkfile.map(_.split(",")(7).trim)

      println("\n===> SPLIT FOR 7th ELEMENT")
      cities.take(20).foreach(println)
    }

    "partitions" in {
      val path: String = Resource.getUrl("matches.csv").getFile

      val sparkfile: RDD[String] = spark.sparkContext.textFile(path)

      val matches: RDD[String] = sparkfile.filter(_ contains "Jaipur")

      println("\n===> PARTITIONS")
      println(matches.partitions.length)
    }
  }
}