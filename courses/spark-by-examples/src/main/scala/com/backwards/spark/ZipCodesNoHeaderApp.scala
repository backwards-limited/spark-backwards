package com.backwards.spark

import scala.io.StdIn
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object ZipCodesNoHeaderApp extends App {
  def spark: IO[Unit] = IO {
    val spark: SparkSession = SparkSession.builder()
      .master("local[3]")
      .appName("zip-codes-no-header")
      .getOrCreate()

    val sc = spark.sparkContext

    val rdd = sc.textFile("./courses/spark-by-examples/src/main/resources/zip-codes-no-header.csv")

    val rdd2: RDD[ZipCode] = rdd.map { row =>
      val strArray = row.split(",")

      ZipCode(strArray(0).toInt, strArray(1), strArray(3), strArray(4))
    }

    rdd2.foreach(z => println(z.city))
  }

  val program = for {
    _ <- spark
    _ <- IO(println("\nView UI and/or hit 'Enter' key to terminate"))
    _ <- IO(StdIn.readChar()).attempt
  } yield ()

  program.unsafeRunSync()
}