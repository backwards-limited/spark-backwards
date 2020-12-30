package com.backwards.spark

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import monocle.macros.syntax.lens._
import org.apache.spark.sql.{Dataset, Row}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import com.backwards.spark.HiveAndPostgesqlExample._
import com.backwards.spark.Spark._

class HiveAndPostgesqlExampleSpec extends AsyncWordSpec with AsyncIOSpec with Matchers {
  "Hive and Postgresql" should {
    "replace nulls" in {
      val scalaCourse   = ("1", "Scala", "David Ainslie", "10")
      val haskellCourse = ("2", "Haskell", null: String, "5")

      val result: IO[Dataset[Row]] = sparkSession(_.appName("hive-and-postgresql-spec").master("local")).use { spark =>
        import spark.implicits._

        replaceNulls(
          List(scalaCourse, haskellCourse).toDF("course_id", "course_name", "author_name", "no_of_reviews")
        ).run(spark)
      }

      result asserting { ds =>
        ds.collect() mustEqual Array(
          Row.fromTuple(scalaCourse),
          Row.fromTuple(haskellCourse.lens(_._3).set("Unknown"))
        )
      }
    }
  }
}