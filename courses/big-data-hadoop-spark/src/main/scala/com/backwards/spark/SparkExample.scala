package com.backwards.spark

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import com.backwards.spark.Spark._

/**
 * {{{
 *  sbt "runMain com.backwards.spark.SparkExample"
 * }}}
 *
 * With Hive support enabled, we'll see something similar to the following in the logs:
 * {{{
 *  SharedState: Setting hive.metastore.warehouse.dir ('null') to the value of spark.sql.warehouse.dir ('file:/Users/davidainslie/workspace/backwards/spark-backwards/spark-warehouse/').
 *  SharedState: Warehouse path is 'file:/Users/davidainslie/workspace/backwards/spark-backwards/spark-warehouse/'
 * }}}
 */
object SparkExample {
  def main(args: Array[String]): Unit =
    program.unsafeRunSync()

  def program: IO[Unit] =
    sparkSession(_.appName("spark-example").master("local").enableHiveSupport()).use { spark =>
      val program =
        for {
          courses <- courses
          _ = courses.show()
          _ <- Kleisli.liftF(IO(courses.write.format("csv").save("samplesq")))
        } yield ()

      program run spark
    }

  def courses: Kleisli[IO, SparkSession, Dataset[Row]] =
    Kleisli(spark => IO delay
      spark.createDataFrame(List(1 -> "Spark", 2 -> "Big Data")).toDF("course id", "course name")
    )
}