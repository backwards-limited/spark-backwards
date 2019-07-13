package com.backwards.spark

import monocle.macros.syntax.lens._
import scopt.OptionParser
import org.apache.spark.sql.SparkSession

/**
  * sbt "runMain com.backwards.spark.GithubArchiveApp --input ./data/input/github-archive/2015-03-01-0.json"
  */
object GithubArchiveApp extends App {
  new OptionParser[Config](s"${getClass.getPackage.getName}.${getClass.getSimpleName}") {
    head("scopt", "4.x")

    opt[String]('i', "input") required() action { (x, c) =>
      c.lens(_.input).set(x)
    } text "input is the input path"

    opt[String]('o', "output") optional() action { (x, c) =>
      c.lens(_.output).set(x)
    } text "output is the output path"
  } parse(args, Config()) foreach run

  lazy val run: Config => Unit = { config =>
    val input = config.input

    val spark = SparkSession.builder()
      .appName("GitHub push counter")
      .master("local[*]") // Uncomment this line when running on local
      .getOrCreate()

    val sc = spark.sparkContext

    val ghLog = spark.read.json(input)

    val pushes = ghLog.filter("type = 'PushEvent'")

    pushes.printSchema
    println("All events: " + ghLog.count)
    println("Only pushes: " + pushes.count)
    pushes.show(5)

    val grouped = pushes.groupBy("actor.login").count
    grouped.show(5)

    val ordered = grouped.orderBy(grouped("count").desc)
    ordered.show(5)
  }
}