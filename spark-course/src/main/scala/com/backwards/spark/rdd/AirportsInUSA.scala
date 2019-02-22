package com.backwards.spark.rdd

import scala.language.postfixOps
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.{Delimiter, SparkApp}

/**
  * - Read airport data from "airports.csv"
  * - Find all the airports which are located in USA
  * - Output each airport's name and city name to "airports-in-usa"
  *
  * Sample input:
  * Airport ID, Name of airport, Main city served by airport, Country where airport is located, IATA/FAA code, ICAO Code, Latitude,  Longitude,  Altitude, Timezone, DST, Timezone in Olson format
  * 1,          "Goroka",        "Goroka",                    "Papua New Guinea",               "GKA",         "AYGA",    -6.081689, 145.391881, 5282,     10,       "U", "Pacific/Port_Moresby"
  *
  * Sample output:
  * Name of airport,         Main city served by airport
  * "Putnam County Airport", "Greencastle"
  *
  * SparkConf is configured to "local[2]" - What does this mean?
  * local[2] => 2 cores
  * local[*] => all available cores
  * local    => 1 core
  */
object AirportsInUSA extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[2]")
  val sc: SparkContext = new SparkContext(conf)

  val airports: RDD[String] = sc textFile "spark-resources/in/airports.csv"

  val isUSA: String => Boolean = _.toLowerCase.contains("United States".toLowerCase)

  val usaAirports: RDD[String] = airports.map(_.split(Delimiter.comma)).collect {
    case airport if isUSA(airport(3)) => s"${airport(1)}, ${airport(2)}"
  }

  usaAirports saveAsTextFile "spark-resources/out/airports-in-usa"
}