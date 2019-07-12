package com.backwards.spark

import monocle.macros.syntax.lens._
import scopt.OptionParser
import org.apache.spark.sql.{SaveMode, SparkSession}

/**
  * sbt "runMain com.backwards.spark.WordCount --input ./data/input/sample.txt --output ./data/output"
  */
object WordCount {
  def main(args: Array[String]): Unit = {
    new OptionParser[Config](s"${getClass.getPackage.getName}.${getClass.getSimpleName}") {
      head("scopt", "4.x")

      opt[String]('i', "input") required() action { (x, c) =>
        c.lens(_.input).set(x)
      } text "input is the input path"

      opt[String]('o', "output") required() action { (x, c) =>
        c.lens(_.output).set(x)
      } text "output is the output path"
    } parse(args, Config()) foreach run

    lazy val run: Config => Unit = { config =>
      val input = config.input
      val output = config.output

      val spark = SparkSession
        .builder()
        //.master("local") // uncomment this line when running on local
        .appName("word-count")
        .getOrCreate()

      import spark.implicits._

      // Read some example file to a test RDD
      val data = spark.read.text(input).as[String]

      val words = data.flatMap(value => value.split("\\s+"))
      val groupWords = words.groupByKey(_.toLowerCase)
      val counts = groupWords.count()
      counts.show()

      counts.coalesce(1).write.mode(SaveMode.Overwrite).csv(output)

      spark.stop
    }
  }
}
