package com.backwards.spark.rdd

import scala.language.postfixOps
import scala.util.Try
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.{Delimiter, SparkApp}

/**
  * - Read airport data from "airports.csv"
  * - Find all the airports whose latitude are bigger than 40
  * - Output each airport's name and latitude to "airports-by-latitude"
  *
  * Sample input:
  * Airport ID, Name of airport, Main city served by airport, Country where airport is located, IATA/FAA code, ICAO Code, Latitude,  Longitude,  Altitude, Timezone, DST, Timezone in Olson format
  * 1,          "Goroka",        "Goroka",                    "Papua New Guinea",               "GKA",         "AYGA",    -6.081689, 145.391881, 5282,     10,       "U", "Pacific/Port_Moresby"
  *
  * Sample output:
  * Name of airport, Latitude
  * "St Anthony",    51.391944
  *
  * SparkConf is configured to "local[2]" - What does this mean?
  * local[2] => 2 cores
  * local[*] => all available cores
  * local    => 1 core
  */
object AirportsByLatitude extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[2]")
  val sc: SparkContext = new SparkContext(conf)

  val airports: RDD[String] = sc textFile "spark-resources/in/airports.csv"

  val gt40: Double => Boolean = _ > 40

  val airportsByLatitude: RDD[String] = airports.map(_.split(Delimiter.comma)).collect {
    case airport if Try(airport(6).toDouble).fold(_ => false, gt40) => s"${airport(1)}, ${airport(6)}"
  }

  airportsByLatitude saveAsTextFile "spark-resources/out/airports-by-latitude"
}