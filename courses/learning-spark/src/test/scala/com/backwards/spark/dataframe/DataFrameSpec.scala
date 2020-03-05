package com.backwards.spark.dataframe

import pprint._
import org.apache.spark.sql.functions.{col, concat, expr}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DataFrameSpec extends AnyWordSpec with Matchers {
  "DataFrame" should {
    // Define a schema for a DataFrame with three named columns author, title, pages.
    "have a schema programmatically defined" in {
      val schema =
        StructType(
          Array(
            StructField("author", StringType, nullable = false),
            StructField("title", StringType, nullable = false),
            StructField("pages", IntegerType, nullable = false)
          )
        )
    }

    "have a schema defined by DDL (defined as a string)" in {
      val schema = "author STRING, title STRING, pages INT"
    }

    "be created from a schema and associated data" in {
      val spark: SparkSession =
        SparkSession.builder.appName("dataframe").config("spark.master", "local").getOrCreate

      val schema: StructType =
        StructType.fromDDL("`Id` INT, `First` STRING, `Last` STRING, `Url` STRING, `Published` STRING, `Hits` INT, `Campaigns` ARRAY<STRING>")

      val data = Seq(
        Row(1, "Jules", "Damji", "https://tinyurl.1", "1/4/2016", 4535, Seq("twitter", "LinkedIn")),
        Row(2, "Brooke","Wenig","https://tinyurl.2", "5/5/2018", 8908, Seq("twitter", "LinkedIn")),
        Row(3, "Denny", "Lee", "https://tinyurl.3","6/7/2019",7659, Seq("web", "twitter", "FB", "LinkedIn")),
        Row(4, "Tathagata", "Das","https://tinyurl.4", "5/12/2018", 10568, Seq("twitter", "FB")),
        Row(5, "Matei","Zaharia", "https://tinyurl.5", "5/14/2014", 40578, Seq("web", "twitter", "FB", "LinkedIn")),
        Row(6, "Reynold", "Xin", "https://tinyurl.6", "3/2/2015", 25568, Seq("twitter", "LinkedIn"))
      )

      val dataFrame: DataFrame = spark.createDataFrame(spark.sparkContext.parallelize(data), schema)
      dataFrame.printSchema()
      dataFrame.show()

      // If you want to use this schema elsewhere in the code, simply execute df.schema and it will return this schema definition:
      pprintln(dataFrame.schema)

      println(dataFrame.columns.mkString(", "))

      // Access a particular column
      println(dataFrame.col("Id"))

      // Use an expression to compute a value
      dataFrame.select(expr("Hits * 2")).show(2)

      // Or use col to compute value
      dataFrame.select(col("Hits") * 2).show(2)

      // Use expression to compute big hitters for blogs - this adds a new column Big Hitters based on the conditional expression
      dataFrame.withColumn("Big Hitters", expr("Hits > 10000")).show()

      // Use expression to concatenate three columns, create a new column, and show the newly created concatenated column
      dataFrame.withColumn("AuthorsId", concat(expr("First"), expr("Last"), expr("Id"))).show()

      // Sort by column "Id" in descending order
      dataFrame.sort(col("Id").desc).show()
    }
  }
}