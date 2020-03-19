package com.backwards.spark.dataset

import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.{Dataset, SparkSession}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DatasetSpec extends AnyWordSpec with Matchers {
  val spark: SparkSession = SparkSession
    .builder
    .appName("Dataset example")
    .config("spark.master", "local")
    .config("spark.some.config.option", "value")
    .getOrCreate

  // For implicit conversions like converting RDDs to DataFrames and SQL encoders (as needed below)
  import spark.implicits._

  // Load the dataset from the CSV with type sales
  val salesDataset: Dataset[Sales] = spark
    .read
    .option("sep", "\t")
    .option("header", "true")
    .csv("data/input/sample_10000.txt")
    .withColumn("id", 'id.cast(IntegerType))
    .as[Sales]

  "Dataset" should {
    "explain" in {
      salesDataset.explain
    }

    "be created from file" in {
      salesDataset.show
    }
  }
}

/**
 * {{{
 *  trait Encoder[T] extends Serializable {
 *    def schema: StructType
 *    def clsTag: ClassTag[T]
 *  }
 * }}}
 *
 * Encoders internally convert type T to Spark SQL's InternalRow type, which is the binary row representation.
 *
 * Define an encoder for this CSV
 */
case class Sales(
  id: Int, firstname: String, lastname: String, address: String, city: String, state: String, zip: String, ip: String, product_id: String, dop: String
)