package com.backwards.spark

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import com.backwards.spark.Collection.syntax._
import com.backwards.spark.Spark._

/**
 * {{{
 *  sbt "runMain com.backwards.spark.PostgresqlExample"
 * }}}
 *
 * This example needs Postgres - There is a docker-compose file that can first be invoked.
 * To interact with the database either use a UI such as DataGrip or from the command line:
 *
 * {{{
 *  psql -h localhost -p 5432 -d world
 * }}}
 */
object PostgesqlExample {
  def main(args: Array[String]): Unit =
    program.unsafeRunSync()

  def program: IO[Unit] =
    sparkSession(_.appName("postgresql-example").master("local").enableHiveSupport()).use { spark =>
      val program =
        for {
          rows <- fetch
          _ = rows.show()
        } yield ()

      program run spark
    }

  def fetch: Kleisli[IO, SparkSession, Dataset[Row]] =
    Kleisli(spark =>
      IO(
        Map(
          "user" -> "jimmy",
          "password" -> "banana"
        )
      ).map(pgConfig =>
        spark.read.jdbc("jdbc:postgresql://localhost:5432/world", "example", pgConfig.toProps)
      )
  )
}