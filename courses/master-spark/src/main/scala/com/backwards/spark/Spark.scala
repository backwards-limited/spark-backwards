package com.backwards.spark

import cats.effect.{IO, Resource}
import org.apache.spark.sql.SparkSession

object Spark {
  def sparkSession(f: SparkSession.Builder => SparkSession.Builder): Resource[IO, SparkSession] = {
    val acquire: IO[SparkSession] =
      IO(f(SparkSession.builder()).getOrCreate())

    val release: SparkSession => IO[Unit] =
      spark => IO(spark.close())

    Resource.make(acquire)(release)
  }
}