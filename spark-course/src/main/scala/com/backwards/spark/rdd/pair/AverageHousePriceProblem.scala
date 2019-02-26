package com.backwards.spark.rdd.pair

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.{Delimiter, SparkApp}

/**
  * Create a Spark program to read the house data from in/RealEstate.csv,
  * output the average price for houses with different number of bedrooms.
  *
  * The houses dataset contains a collection of recent real estate listings in San Luis Obispo county and around it. 
  *
  * The dataset contains the following fields:
  *     1. MLS: Multiple listing service number for the house (unique ID).
  *     2. Location: city/town where the house is located.
  *        Most locations are in San Luis Obispo county and northern Santa Barbara county (Santa Maria­Orcutt, Lompoc, Guadelupe, Los Alamos),
  *        but there some out of area locations as well.
  *     3. Price: the most recent listing price of the house (in dollars).
  *     4. Bedrooms: number of bedrooms.
  *     5. Bathrooms: number of bathrooms.
  *     6. Size: size of the house in square feet.
  *     7. Price/SQ.ft: price of the house per square foot.
  *     8. Status: type of sale. Thee types are represented in the dataset: Short Sale, Foreclosure and Regular.
  *
  * Each field is comma separated.
  *
  * Sample output:
  *
  * (3, 325000)
  * (1, 266356)
  * (2, 325000)
  * ...
  *
  * 3, 1 and 2 mean the number of bedrooms. 325000 means the average price of houses with 3 bedrooms is 325000.
  */
object AverageHousePriceProblem extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[3]")
  val sc: SparkContext = new SparkContext(conf)

  val lines = sc textFile "spark-resources/in/real-estate.csv"

  val bedroomsAndCountedPrices: RDD[(Int, (Int, Double))] = lines.filter(!_.contains("Bedrooms")).map { h =>
    val cols = h split Delimiter.comma
    val (bedroomCount, price) = cols(3).toInt -> cols(2).toDouble
    bedroomCount -> (1, price)
  }.reduceByKey { case ((count1, price1), (count2, price2)) =>
    (count1 + count2) -> (price1 + price2)
  }

  println

  bedroomsAndCountedPrices.collect().foreach { case (bedrooms, (count, totalPrice)) =>
    println(s"Bedrooms: $bedrooms, count = $count, total price = $totalPrice")
  }

  val bedroomsAveragePrice: RDD[(Int, Double)] = bedroomsAndCountedPrices.mapValues { case (count, totalPrice) =>
    totalPrice / count
  }

  println

  bedroomsAveragePrice.collect().foreach { case (bedrooms, averagePrice) =>
    println(s"Bedrooms: $bedrooms, average price = $averagePrice")
  }
}