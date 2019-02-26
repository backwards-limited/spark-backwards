package com.backwards.spark.rdd.pair

import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.SparkApp

object AverageHousePriceSorted extends SparkApp {
  val conf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[3]")
  val sc = new SparkContext(conf)

  val lines = sc.textFile("spark-resources/in/real-estate.csv")
  val cleanedLines = lines.filter(line => !line.contains("Bedrooms"))

  val housePricePairRdd = cleanedLines.map { line =>
    (line.split(",")(3).toInt, AvgCount(1, line.split(",")(2).toDouble))
  }

  val housePriceTotal = housePricePairRdd.reduceByKey((x, y) => AvgCount(x.count + y.count, x.total + y.total))

  val housePriceAvg = housePriceTotal.mapValues(avgCount => avgCount.total / avgCount.count)

  val sortedHousePriceAvg = housePriceAvg.sortByKey(ascending = false)

  for ((bedrooms, avgPrice) <- sortedHousePriceAvg.collect()) println(bedrooms + " : " + avgPrice)
}

case class AvgCount(count: Int, total: Double)