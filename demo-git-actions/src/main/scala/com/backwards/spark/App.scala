package com.backwards.spark

import cats.data.Kleisli
import cats.effect.IO
import cats.implicits._
import org.apache.spark.sql.streaming.StreamingQuery
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import com.backwards.spark.Spark._

object App {
  def main(args: Array[String]): Unit =
    sparkSession(_.appName("7b").master("local")).use(program.run).unsafeRunSync()

  def program: Kleisli[IO, SparkSession, Unit] =
    Kleisli { spark =>
      IO {
        val sequence = Seq("One", "Two", "Three")
        val rdd = spark.sparkContext.parallelize(sequence)
        rdd.saveAsTextFile("s3://bucket_name/folder_name")
      }
    }
}