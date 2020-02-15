package com.backwards.spark

import scala.io.Source.fromFile
import monocle.macros.syntax.lens._
import scopt.OptionParser
import org.apache.spark.sql.SparkSession

/**
  * sbt "runMain com.backwards.spark.GithubAllArchiveApp --input ./data/input/github-archive --employees ./data/input/github-archive/gh-employees.txt --format json --output ./data/output/github-archive"
  */
object GithubAllArchiveApp extends App {
  final case class Config(input: String = "", employees: String = "", format: String = "", output: String = "")

  new OptionParser[Config](s"${getClass.getPackage.getName}.${getClass.getSimpleName}") {
    head("scopt", "4.x")

    opt[String]('i', "input") required() action { (x, c) =>
      c.lens(_.input).set(x)
    } text "input is the input path"

    opt[String]('e', "employees") required() action { (x, c) =>
      c.lens(_.employees).set(x)
    } text "employees is the employees path"

    opt[String]('f', "format") required() action { (x, c) =>
      c.lens(_.format).set(x)
    } text "format is the format"

    opt[String]('o', "output") required() action { (x, c) =>
      c.lens(_.output).set(x)
    } text "output is the output path"
  } parse(args, Config()) foreach run

  lazy val run: Config => Unit = { config =>
    //val spark = SparkSession.builder().getOrCreate()
    val spark = SparkSession.builder()
      .appName("GitHub push counter")
      .master("local[*]") // Uncomment this line when running on local
      .getOrCreate()

    val sc = spark.sparkContext

    val ghLog = spark.read.json(s"${config.input}/*.json")
    val pushes = ghLog.filter("type = 'PushEvent'")
    val grouped = pushes.groupBy("actor.login").count
    val ordered = grouped.orderBy(grouped("count").desc)

    val employees = Set() ++ fromFile(config.employees).getLines.map(_.trim)
    val bcEmployees = sc.broadcast(employees)

    import spark.implicits._

    val isEmp: String => Boolean = bcEmployees.value.contains

    val sqlFunc = spark.udf.register("SetContainsUdf", isEmp)

    val filtered = ordered.filter(sqlFunc($"login"))
    filtered.write.format(config.format).save(config.output)
  }
}