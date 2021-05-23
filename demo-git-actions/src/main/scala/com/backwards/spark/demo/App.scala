package com.backwards.spark.demo

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import org.apache.spark.sql.SparkSession
import com.backwards.spark.Spark._

object App {
  def main(args: Array[String]): Unit =
    sparkResource(_.appName("7b").master("local")).use(program.run).unsafeRunSync()

  def program: Kleisli[IO, SparkSession, Unit] =
    Kleisli { spark =>
      IO {
        val sequence = Seq("One", "Two", "Three")
        val rdd = spark.sparkContext.parallelize(sequence)
        rdd.saveAsTextFile("s3://bucket_name/folder_name")
      }
    }
}