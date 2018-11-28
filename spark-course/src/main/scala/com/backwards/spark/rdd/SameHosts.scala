package com.backwards.spark.rdd

import scala.language.postfixOps
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.{Delimiter, SparkApp}

/**
  * "nasa-19950701.tsv" file contains 10000 log lines from one of NASA's apache server for July 1st, 1995.
  * "nasa-19950801.tsv" file contains 10000 log lines for August 1st, 1995.
  * - Generate a new RDD which contains the hosts which are accessed on BOTH days
  * - Save to "nasa-same-hosts-logs"
  * - Note, the original log files contain the following header lines, which need to be removed:
  *   host	logname	time	method	url	response	bytes
  * - Also note that we'll set master to use just 1 thread (CPU core) locally.
  */
object SameHosts extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[1]")
  val sc: SparkContext = new SparkContext(conf)

  val host: RDD[String] => RDD[String] =
    _.map(_.split(Delimiter.tab)(0))

  val hosts = host andThen dropFirstLine

  val `july 1st`: RDD[String] = hosts(sc textFile "spark-resources/in/nasa-19950701.tsv")

  val `august 1st`: RDD[String] = hosts(sc textFile "spark-resources/in/nasa-19950801.tsv")

  (`july 1st` intersection `august 1st`) saveAsTextFile "spark-resources/out/nasa-same-hosts-logs"
}