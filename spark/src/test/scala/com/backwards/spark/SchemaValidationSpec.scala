package com.backwards.spark

import scala.language.postfixOps
import better.files.Resource.{getUrl => resourceUrl}
import cats.data.Kleisli
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import org.apache.spark.sql.types.DataTypes.{IntegerType, LongType}
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.sql.{Dataset, SparkSession}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.backwards.spark.SparkDeprecated._
import com.backwards.spark.typelevel.Attributes

class SchemaValidationSpec extends AnyWordSpec with Matchers {
  val blahHardCoded: Kleisli[IO, SparkSession, Dataset[Blah]] =
    Kleisli { spark =>
      import spark.implicits._

      IO(
        spark.sparkContext.parallelize(
          List(
            (6, 66),
            (8, 88)
          )
        )
      ).map(_.toDF(Attributes[Blah]: _*).as[Blah])
    }

  val blahCsv: Kleisli[IO, SparkSession, Dataset[Blah]] =
    Kleisli { spark =>
      import spark.implicits._

      val schema: StructType =
        StructType(
          List(
            StructField("myint", IntegerType, nullable = false),
            StructField("mylong", LongType, nullable = true)
          )
        )

      IO(
        spark.read
          .option("header", "false")
          .schema(schema)
          .csv(resourceUrl("blah.csv").getFile).as[Blah]
      )
    }

  "" should {
    "" in {
      val program: Kleisli[IO, SparkSession, (Dataset[Blah], Dataset[Blah])] =
        for {
          b1 <- blahHardCoded
          b2 <- blahCsv
        } yield (b1, b2)

      val (blahs1: List[Blah], blahs2: List[Blah]) =
        sparkSession(_.appName("test").master("local")).use(program.run).map {
          case (ds1, ds2) => (ds1.collect.toList, ds2.collect.toList)
        }.unsafeRunSync()

      pprint.pprintln(blahs1)
      pprint.pprintln(blahs2)
    }
  }
}

final case class Blah(myint: Int, mylong: Long)