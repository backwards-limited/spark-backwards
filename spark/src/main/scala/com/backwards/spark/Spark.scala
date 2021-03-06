package com.backwards.spark

import scala.language.higherKinds
import cats.effect.{IO, Resource, Sync}
import cats.implicits._
import org.apache.spark.sql.SparkSession

// TODO - Monadic logging
object Spark {
  def sparkResource(f: SparkSession.Builder => SparkSession.Builder): Resource[IO, SparkSession] = {
    val acquire: IO[SparkSession] =
      IO(println("Aquiring Spark Session")) >> IO(f(SparkSession.builder).getOrCreate)

    val release: SparkSession => IO[Unit] =
      spark => IO(println("Closing Spark Session")) >> IO(spark.close())

    Resource.make(acquire)(release)
  }

  // TODO - Might replace the above with this
  def sparkResource2[F[_]: Sync](f: SparkSession.Builder => SparkSession.Builder): Resource[F, SparkSession] = {
    val acquire: F[SparkSession] =
      Sync[F].delay(println("Aquiring Spark Session")) >> Sync[F].delay(f(SparkSession.builder).getOrCreate)

    val release: SparkSession => F[Unit] =
      spark => Sync[F].delay(println("Closing Spark Session")) >> Sync[F].delay(spark.close())

    Resource.make(acquire)(release)
  }
}