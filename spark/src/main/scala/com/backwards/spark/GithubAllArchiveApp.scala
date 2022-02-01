package com.backwards.spark

import scala.io.Source.fromFile
import scala.util.Using
import scopt.OptionParser
import org.apache.spark.sql.SparkSession
import com.backwards.spark.Config._

/**
  * sbt "runMain com.backwards.spark.GithubAllArchiveApp --input ./data/input/github-archive --employees ./data/input/github-archive/gh-employees.txt --format json --output ./data/output/github-archive"
  */
object GithubAllArchiveApp extends App {
  new OptionParser[Config](s"${getClass.getPackage.getName}.${getClass.getSimpleName}") {
    head("scopt", "4.x")

    opt[String]('i', "input").required().action((x, c) =>
      inputL.set(x)(c)
    ).text("input is the input path")

    opt[String]('e', "employees").required().action((x, c) =>
      employeesL.set(x)(c)
    ).text("employees is the employees path")

    opt[String]('f', "format").required().action((x, c) =>
      formatL.set(x)(c)
    ).text("format is the format")

    opt[String]('o', "output").optional().action((x, c) =>
      outputL.set(x)(c)
    ).text("output is the output path")
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
    val grouped = pushes.groupBy("actor.login").count()
    val ordered = grouped.orderBy(grouped("count").desc)

    val employees: Set[String] =
      Set() ++ Using.resource(fromFile(config.employees))(_.getLines().map(_.trim))

    val bcEmployees = sc.broadcast(employees)

    import spark.implicits._

    val isEmp: String => Boolean = bcEmployees.value.contains

    val sqlFunc = spark.udf.register("SetContainsUdf", isEmp)

    val filtered = ordered.filter(sqlFunc($"login"))
    filtered.write.format(config.format).save(config.output)
  }
}