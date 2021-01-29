package com.backwards.spark

import scala.language.postfixOps
import scala.reflect.ClassTag
import cats.effect.{IO, Resource}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.{DataFrame, Dataset, Encoder, Encoders, Row, SparkSession}
import org.apache.spark.sql.types.DataTypes.{DateType, IntegerType, LongType, StringType}
import org.apache.spark.sql.types.{StructField, StructType}
import org.scalacheck.util.SerializableCanBuildFroms.listCanBuildFrom
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.backwards.spark.Spark._
import com.backwards.spark.typelevel.Attributes
import better.files.Resource.{getUrl => resourceUrl}
import cats.data.Kleisli
import cats.implicits._

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
      ).map(rdd => rdd.toDF(Attributes[Blah]: _*).as[Blah])
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
        sparkSession(_.appName("test").master("local"))
          .flatMap(ss =>
            Resource.liftF(program run ss map {
              case (ds1, ds2) =>
                (ds1.collect.toList, ds2.collect.toList)
            })
          )
          .use(_.pure[IO])
          .unsafeRunSync

      pprint.pprintln(blahs1)
      pprint.pprintln(blahs2)
    }
  }
}

final case class Blah(myint: Int, mylong: Long)