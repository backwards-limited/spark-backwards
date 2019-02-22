package com.backwards.spark.rdd.pair

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.{Delimiter, SparkApp}

/**
  * Create a Spark program to read the airport data from in/airports.txt;
  * generate a pair RDD with airport name being the key and country name being the value.
  * Then remove all the airports which are located in United States and output the pair RDD to out/airports-not-in-usa-pair-rdd.txt
  *
  * Each row of the input file contains the following columns:
  * Airport ID, Name of airport, Main city served by airport, Country where airport is located, IATA/FAA code, ICAO Code, Latitude, Longitude, Altitude, Timezone, DST, Timezone in Olson format
  *
  * Sample output:
  *
  * ("Kamloops", "Canada")
  * ("Wewak Intl", "Papua New Guinea")
  * ...
  */
object AirportsNotInUsaProblem extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[1]")
  val sc: SparkContext = new SparkContext(conf)

  val airports: RDD[String] = sc textFile "spark-resources/in/airports.txt"

  val isUSA: String => Boolean = _.toLowerCase.contains("United States".toLowerCase)

  val airportKeyValuesNotInUSA = airports.map { a =>
    val cols = a split Delimiter.comma
    cols(1) -> cols(3)
  }.filter { case (_, country) =>
    !isUSA(country)
  }

  airportKeyValuesNotInUSA saveAsTextFile "spark-resources/out/airports-not-in-usa-pair-rdd.txt"
}