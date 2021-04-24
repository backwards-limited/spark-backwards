package com.backwards.spark

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import org.apache.spark.sql.{Dataset, Row, SaveMode, SparkSession}
import com.backwards.spark.Spark._

/**
 * {{{
 *  sbt "runMain com.backwards.spark.HiveAndPostgesqlExample"
 * }}}
 *
 * This example needs Postgres - There is a docker-compose file that can first be invoked.
 * To interact with the database either use a UI such as DataGrip or from the command line:
 *
 * {{{
 *  psql -h localhost -p 5432 -d world
 * }}}
 */
object HiveAndPostgesqlExample {
  def main(args: Array[String]): Unit =
    program.unsafeRunSync()

  def program: IO[Unit] =
    sparkSession(_.appName("hive-postgresql-example").master("local").enableHiveSupport()).use { spark =>
      val program =
        for {
          _ <- createHiveTable
          coursesWithNulls <- readHiveTable
          _ = coursesWithNulls.show()
          courses <- replaceNulls(coursesWithNulls)
          _ = courses.show()
          _ <- persist(courses)
          _ <- writeHiveTable(courses)
        } yield ()

      program run spark
    }

  /**
   * When this is executed, we'll see entries under the spark-warehouse folder (within the project root).
   */
  def createHiveTable: Kleisli[IO, SparkSession, Unit] =
    Kleisli { spark =>
      import spark._

      IO(sql("create database if not exists coursesdb")) *>
      IO(sql("create table if not exists coursesdb.courses(course_id string, course_name string, author_name string, no_of_reviews string)")) *>
      IO(sql("insert into coursesdb.courses values('1', 'Java', 'Backwards', '45')")) *>
      IO(sql("insert into coursesdb.courses values('2', 'Java', 'Backwards', '56')")) *>
      IO(sql("insert into coursesdb.courses values('3', 'Big Data', 'Backwards', '100')")) *>
      IO(sql("insert into coursesdb.courses values('4', 'Linux', 'Backwards', '100')")) *>
      IO(sql("insert into coursesdb.courses values('5', 'Microservices', 'Backwards', '100')")) *>
      IO(sql("insert into coursesdb.courses values('6', 'CMS', '', '100')")) *>
      IO(sql("insert into coursesdb.courses values('7', 'Python', 'Backwards', '')")) *>
      IO(sql("insert into coursesdb.courses values('8', 'CMS', 'Backwards', '56')")) *>
      IO(sql("insert into coursesdb.courses values('9', 'Dot Net', 'Backwards', '34')")) *>
      IO(sql("insert into coursesdb.courses values('10', 'Ansible', 'Backwards', '123')")) *>
      IO(sql("insert into coursesdb.courses values('11', 'Jenkins', 'Backwards', '32')")) *>
      IO(sql("insert into coursesdb.courses values('12', 'Chef', 'Backwards', '121')")) *>
      IO(sql("insert into coursesdb.courses values('13', 'Go Lang', '', '105')")) *>
      // Treat empty strings as null
      IO(sql("alter table coursesdb.courses set tblproperties('serialization.null.format' = '')"))
    }

  def readHiveTable: Kleisli[IO, SparkSession, Dataset[Row]] =
    Kleisli(spark =>
      IO(spark.sql("select * from coursesdb.courses"))
    )

  def replaceNulls: Dataset[Row] => Kleisli[IO, SparkSession, Dataset[Row]] =
    ds => Kleisli liftF IO(
      ds.na.fill("Unknown", List("author_name"))
        .na.fill("0", List("no_of_reviews"))
    )

  def persist: Dataset[Row] => Kleisli[IO, SparkSession, Unit] =
    ds => Kleisli liftF IO(
      ds.write
        .mode(SaveMode.Append)
        .format("jdbc")
        .option("url", "jdbc:postgresql://localhost:5432/world")
        .option("dbtable", "courses")
        .option("user", "jimmy")
        .option("password", "banana")
        .save()
    )

  def writeHiveTable: Dataset[Row] => Kleisli[IO, SparkSession, Unit] = {
    val coursesTransformedTempView = "coursesTransformedTempView"

    ds => Kleisli(spark =>
      IO(ds.createOrReplaceTempView(coursesTransformedTempView)) *>
      IO(spark.sql(s"create table coursesdb.coursesTransformed as select * from $coursesTransformedTempView"))
    )
  }
}