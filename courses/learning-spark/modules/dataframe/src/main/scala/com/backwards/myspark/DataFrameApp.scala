package com.backwards.myspark

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object DataFrameApp {
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("dataframe-app")
      .master("local[1]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    import spark.implicits._

    val jsonFile = "courses/learning-spark/modules/dataframe/data/blogs.json"

    // Define our schema as before
    val schema: StructType =
      StructType(
        Array(
          StructField("Id", IntegerType, nullable = false),
          StructField("First", StringType, nullable = false),
          StructField("Last", StringType, nullable = false),
          StructField("Url", StringType, nullable = false),
          StructField("Published", StringType, nullable = false),
          StructField("Hits", IntegerType, nullable = false),
          StructField("Campaigns", ArrayType(StringType), nullable = false)
        )
      )

    // Create a DataFrame by reading from the JSON file a predefined Schema
    val blogsDF = spark.read.schema(schema).json(jsonFile)

    // Show the DataFrame schema as output
    blogsDF.show(truncate = false)

    // Print the schemas
    println(blogsDF.printSchema)
    println(blogsDF.schema)

    // Show columns and expressions
    blogsDF.select(expr("Hits") * 2).show(2)
    blogsDF.select(col("Hits") * 2).show(2)
    blogsDF.select(expr("Hits * 2")).show(2)

    // Show heavy hitters
    blogsDF.withColumn("Big Hitters", expr("Hits > 10000")).show()

    // Concatenate three columns, create a new column, and show the newly created concatenated column
    blogsDF
      .withColumn("AuthorsId", concat(expr("First"), expr("Last"), expr("Id")))
      .select(col("AuthorsId"))
      .show(4)

    // Sort by column "Id" in descending order (where the following two are equivalent)
    blogsDF.sort(col("Id").desc).show()
    blogsDF.sort($"Id".desc).show()
  }
}