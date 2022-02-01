package com.backwards.spark

import scala.io.Source.fromFile
import scala.util.Using
import scopt.OptionParser
import org.apache.spark.sql.SparkSession
import com.backwards.spark.Config._

/**
  * sbt "runMain com.backwards.spark.GithubArchiveApp --input ./data/input/github-archive/2015-03-01-0.json --employees ./data/input/github-archive/gh-employees.txt"
  */
object GithubArchiveApp extends App {
  new OptionParser[Config](s"${getClass.getPackage.getName}.${getClass.getSimpleName}") {
    head("scopt", "4.x")

    opt[String]('i', "input").required().action((x, c) =>
      inputL.set(x)(c)
    ).text("input is the input path")

    opt[String]('e', "employees").required().action((x, c) =>
      employeesL.set(x)(c)
    ).text("employees is the employees path")

    opt[String]('o', "output").optional().action((x, c) =>
      outputL.set(x)(c)
    ).text("output is the output path")
  } parse(args, Config()) foreach run

  lazy val run: Config => Unit = { config =>
    val spark = SparkSession.builder()
      .appName("GitHub push counter")
      .master("local[*]") // Uncomment this line when running on local
      .getOrCreate()

    import spark.implicits._

    val sc = spark.sparkContext

    val ghLog = spark.read.json(config.input)

    val pushes = ghLog.filter("type = 'PushEvent'")

    pushes.printSchema()
    println("All events: " + ghLog.count())
    println("Only pushes: " + pushes.count())
    pushes.show(5)

    val grouped = pushes.groupBy("actor.login").count()
    grouped.show(5)

    val ordered = grouped.orderBy(grouped("count").desc)
    ordered.show(5)

    val employees: Set[String] =
      Set() ++ Using.resource(fromFile(config.employees))(_.getLines().map(_.trim))

    val bcEmployees = sc broadcast employees

    val isEmp: String => Boolean = bcEmployees.value.contains

    // Register isEmp as a UDF (user defined function):
    val isEmployee = spark.udf.register("isEmpUdf", isEmp)

    val filtered = ordered.filter(isEmployee($"login"))
    filtered.show()
  }
}