package com.backwards.spark

import scala.util.chaining._
import better.files.Resource.{getUrl => resourceUrl}
import cats.effect.IO
import cats.implicits._
import com.backwards.spark.Spark._

object _4b {
  val boringWords: String = """(
    'a', 'an', 'and', 'are', 'as', 'at', 'be', 'but', 'by',
    'for', 'if', 'in', 'into', 'is', 'it',
    'no', 'not', 'of', 'on', 'or', 'such',
    'that', 'the', 'their', 'then', 'there', 'these',
    'they', 'this', 'to', 'was', 'will', 'with', 'he', 'she',
    'your', 'you', 'I',
    'i', '[', ']', '[]', 'his', 'him', 'our', 'we'
  )"""

  def main(args: Array[String]): Unit =
    program.unsafeRunSync()

  def program: IO[Unit] =
    sparkSession(_.appName("4b").master("local")).use { spark =>
      import spark.implicits._

      IO delay
        spark.read.format("text").load(resourceUrl("shakespeare.txt").getFile)
          .flatMap(_.toString.split(" ").toIterator)
          .toDF() // To use Tungsten under the hood
          .tap(_.printSchema()).tap(_.show(10))
          .filter(s"lower(value) NOT IN $boringWords")
          .groupBy("value").count()
          .pipe(ds => ds.orderBy(ds.col("count").desc))
          .show(200)
    }
}
