package com.backwards.spark

import java.net.URL
import better.files.Resource.{getUrl => resourceUrl}
import cats.data.Kleisli
import cats.effect.IO
import cats.implicits._
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import com.backwards.spark.Spark._

/**
 * Note on below "filter" - we could instead use:
 * {{{
 *  filter(
 *    (gradeChart.col("gpa").gt(3.0) and gradeChart.col("gpa").lt(4.5)) or
 *    gradeChart.col("gpa").equalTo(1.0)
 *  )
 * }}}
 */
object _5 {
  def main(args: Array[String]): Unit =
    program.unsafeRunSync()

  def program: IO[Unit] =
    sparkSession(_.appName("4a").master("local")).use { spark =>
      val program: Kleisli[IO, SparkSession, Unit] = {
        for {
          students <- load(resourceUrl("students.csv"))
          gradeChart <- load(resourceUrl("grade-chart.csv"))
        } yield
          students.join(gradeChart, students.col("GPA").equalTo(gradeChart.col("gpa")))
            .filter(gradeChart.col("gpa").between(3.5, 4.5) or gradeChart.col("gpa").equalTo(1.0))
            .select("student_name", "favorite_book_title", "letter_grade")
            .show()
      }

      program run spark
    }

  def load(url: URL): Kleisli[IO, SparkSession, Dataset[Row]] =
    Kleisli { spark =>
      IO delay
        spark.read.format("csv")
          .option("inferschema", value = "true")
          .option("header", value = true)
          .load(url.getFile)
    }
}
