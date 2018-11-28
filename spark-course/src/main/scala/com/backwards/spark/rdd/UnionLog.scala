package com.backwards.spark.rdd

import scala.language.postfixOps
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import com.backwards.spark.SparkApp

/**
  * "nasa-19950701.tsv" file contains 10000 log lines from one of NASA's apache server for July 1st, 1995.
  * "nasa-19950801.tsv" file contains 10000 log lines for August 1st, 1995.
  * - Generate a new RDD which contains the log lines from both July 1st and August 1st
  * - Take a 0.1 sample of those log lines
  * - Save to "nasa-sample-logs"
  * - Note, the original log files contain the following header lines, which need to be removed:
  *   host	logname	time	method	url	response	bytes
  * - Also note that we'll set master to "local[*]" to utilise all available CPU cores.
  */
object UnionLog extends SparkApp {
  val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[*]")
  val sc: SparkContext = new SparkContext(conf)

  val `july 1st`: RDD[String] = dropFirstLine(sc textFile "spark-resources/in/nasa-19950701.tsv")

  val `august 1st`: RDD[String] = dropFirstLine(sc textFile "spark-resources/in/nasa-19950801.tsv")

  val sampleLogs: RDD[String] = `july 1st`.union(`august 1st`).sample(withReplacement = true, fraction = 0.1)

  sampleLogs saveAsTextFile "spark-resources/out/nasa-sample-logs"
}