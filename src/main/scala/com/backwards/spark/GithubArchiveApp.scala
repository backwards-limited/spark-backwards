package com.backwards.spark

import monocle.macros.syntax.lens._
import org.apache.spark.sql.SparkSession

/**
  * sbt "; project spark-in-action; runMain com.backwards.spark.GithubArchiveApp --input my-input --output my-output"
  */
object GithubArchiveApp extends App {
  final case class Config(input: String = "", output: String = "")

  val spark = SparkSession.builder()
    .appName("GitHub push counter")
    .master("local[*]")
    .getOrCreate()

  val sc = spark.sparkContext


  /*val homeDir = System.getenv("HOME")
  val inputPath = homeDir + "/sia/github-archive/2015-03-01-0.json"
  val ghLog = spark.read.json(inputPath)

  val pushes = ghLog.filter("type = 'PushEvent'")*/

  val parser = new scopt.OptionParser[Config](getClass.getName) {
    head("scopt", "4.x")

    opt[String]('f', "input") required() action { (x, c) =>
      c.lens(_.input).set(x)
    } text "input is the input path"

    opt[String]('o', "output") required() action { (x, c) =>
      c.lens(_.input).set(x)
    } text "output is the output path"
  }

  // parser.parse returns Option[C]
  parser.parse(args, Config()) foreach { config =>
    // do stuff
    val input = config.input
    val output = config.output
    println("input==" + input)
    println("output=" + output)

    /*val sc = new SparkContext()
    val rdd = sc.textFile(input)
    rdd.saveAsTextFile(output)*/

  }
}
