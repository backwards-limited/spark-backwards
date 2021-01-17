package com.backwards.spark

import scala.reflect.ClassTag
import cats.effect.{IO, Resource}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.{DataFrame, Dataset, Encoder, Encoders, Row}
import org.apache.spark.sql.types.DataTypes.{DateType, IntegerType, LongType, StringType}
import org.apache.spark.sql.types.{StructField, StructType}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.backwards.spark.Spark._
import com.backwards.spark.typelevel.Attributes

class SchemaValidationSpec extends AnyWordSpec with Matchers {
  "" should {
    "" in {
      val schema: StructType =
        StructType(
          List(
            StructField("myint", IntegerType, nullable = false),
            StructField("mylong", LongType, nullable = true)
          )
        )

      val data =
        List(
          Row(8, 8),
          Row(64, 64),
        )

      val program = {
        for {
          spark <- sparkSession(_.appName("test").master("local"))
          df <- Resource.liftF {
            import spark.implicits._

            /*val x = new Encoder[Long] {
              def schema: StructType = ???

              def clsTag: ClassTag[Long] = ???
            }*/

            /*implicit def x = Encoders.scalaInt
            implicit def y = Encoders.scalaLong*/

            /*implicit val tupleEncoder =
              Encoders.tuple(Encoders.scalaInt, Encoders.scalaLong)*/

            //implicit val def x = spar

            //implicit def b = Encoders.product[Blah]


            val v: RDD[(Int, Int)] = spark.sparkContext.parallelize(
              List(
                (8, 88)
              )
            )

            println(v.toDF(Attributes[Blah].fieldNames: _*).as[Blah])


            val x: DataFrame =
              spark.read
                .option("header", "false")
                .schema(schema)
                .csv("/Users/davidainslie/workspace/backwards/spark-backwards/spark/src/test/resources/blah.csv")

            println(x.collect().toList.mkString(", "))

            val y: Dataset[Blah] = x.as[Blah]

            IO(
              /*spark.createDataFrame(
                spark.sparkContext.parallelize(data),
                schema
              ).as[Blah]*/
              x.as[Blah]
            )
          }
        } yield df.collect.toList
      }

      val result: List[Blah] =
        program.use(r => IO(r)).unsafeRunSync

      pprint.pprintln(result)
    }
  }
}

final case class Blah(myint: Int, mylong: Long)