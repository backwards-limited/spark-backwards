package com.backwards.spark.rdd.pair

import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.{Delimiter, SparkApp}

/**
  * Create a Spark program to read the airport data from in/airports.txt,
  * output the the list of the names of the airports located in each country.
  *
  * Each row of the input file contains the following columns:
  * Airport ID, Name of airport, Main city served by airport, Country where airport is located, IATA/FAA code, ICAO Code, Latitude, Longitude, Altitude, Timezone, DST, Timezone in Olson format
  *
  * Sample output:
  *
  * "Canada", List("Bagotville", "Montreal", "Coronation", ...)
  * "Norway" : List("Vigra", "Andenes", "Alta", "Bomoen", "Bronnoy",..)
  * "Papua New Guinea",  List("Goroka", "Madang", ...)
  * ...
  */
object AirportsByCountryProblem extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[3]")
  val sc: SparkContext = new SparkContext(conf)

  val lines = sc textFile "spark-resources/in/airports.txt"

  val airportsByCountry = lines.map { a =>
    val cols = a split Delimiter.comma
    cols(3) -> cols(1)
  }.groupByKey()

  airportsByCountry.collect().foreach { case (country, cities) =>
    println(s"$country: ${cities.mkString(", ")}")
  }
}