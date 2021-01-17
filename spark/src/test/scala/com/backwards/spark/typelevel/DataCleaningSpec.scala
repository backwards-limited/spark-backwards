package com.backwards.spark.typelevel

import org.apache.spark.sql.{DataFrameReader, Dataset, Encoders, Row, SparkSession}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.reflect.runtime.universe._
import com.backwards.NotNothing

/**
 * [[https://www.franks.codes/post/2018-03-02-generic-derivation-for-spark-data-cleaning/ Using Shapeless for Data Cleaning in Spark]]
 */
class DataCleaningSpec extends AnyWordSpec with Matchers {
  implicit val spark: SparkSession = {
    SparkSession
      .builder()
      .master("local")
      .appName("test")
      .getOrCreate()
  }

  def importData[A <: Product: TypeTag: NotNothing, B <: Product: TypeTag: NotNothing](
    importFn: DataFrameReader => Dataset[Row], filterFn: Dataset[Row] => Row => Boolean = _ => _ => true
  )(implicit spark: SparkSession, projector: PartialProjector[A, B]): Dataset[B] = {
    import spark.implicits._

    val schema = Encoders.product[A].schema

    val reader: DataFrameReader = spark.read.schema(schema)
    val ds: Dataset[Row] = importFn(reader)
    val filterPred: Row => Boolean = filterFn(ds)
    val filteredDs: Dataset[A] = ds.filter(filterPred).as[A]

    filteredDs.mapPartitions(
      _.map(projector.enforceNotNulls).collect {
        case Some(x) => x
      }
    )
  }

  "" should {
    "x" in {
      pprint.pprintln(PartialProjector[String, String].enforceNotNulls("foo"))
      pprint.pprintln(PartialProjector[String, String].enforceNotNulls(null))
    }

    "y" in {
      final case class A(normal: String, mandatory: Option[String], opt: Option[String])

      final case class B(normal: String, mandatory: String, opt: Option[String])

      def in1 = A("foo", Some("bar"), Some("baz"))
      def in2 = A("foo", None, Some("baz"))

      val res1: Option[B] = PartialProjector[A, B].enforceNotNulls(in1) // Some(B("foo", "bar", "baz"))
      pprint.pprintln(res1)

      val res2: Option[B] = PartialProjector[A, B].enforceNotNulls(in2) // None
      pprint.pprintln(res2)
    }

    /*"z" in {
      case class RawGroup(id: Option[String], isActive: Option[Boolean], description: Option[String])

      case class Group(id: String, isActive: Boolean, description: Option[String])

      val import1: Dataset[Group] =
        importData[RawGroup, Group](
          _.option("header", "true").csv("/data/some.csv").withColumnRenamed("Strange col N4m€", "colName").filter("colName is not null")
        )
    }*/
  }
}

