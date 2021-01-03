package com.backwards.spark

import monocle.macros.syntax.lens._
import scopt.OptionParser
import org.apache.spark.sql.{Dataset, KeyValueGroupedDataset, SaveMode, SparkSession}

/**
  * sbt "runMain com.backwards.spark.WordCountApp --input ./data/input/sample.txt --output ./data/output"
  */
object WordCountApp {
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

      val spark: SparkSession = SparkSession
        .builder()
        //.master("local") // Uncomment this line when running on local
        .appName("word-count")
        .getOrCreate()

      import spark.implicits._

      // Read some example file to a test RDD
      val data: Dataset[String] = spark.read.text(input).as[String]

      val words: Dataset[String] = data.flatMap(value => value.split("\\s+"))
      val groupWords: KeyValueGroupedDataset[String, String] = words.groupByKey(_.toLowerCase)
      val counts: Dataset[(String, Long)] = groupWords.count()
      counts.show()

      counts.coalesce(1).write.mode(SaveMode.Overwrite).csv(output)

      spark.stop
    }
  }
}