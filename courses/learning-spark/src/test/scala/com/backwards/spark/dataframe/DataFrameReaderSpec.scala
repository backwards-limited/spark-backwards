package com.backwards.spark.dataframe

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class DataFrameReaderSpec extends AnyFreeSpec with Matchers {
  "" - {
    val fileSchema =
      StructType(
        Array(
          StructField("CallNumber", IntegerType, nullable = true),
          StructField("UnitID", StringType, nullable = true),
          StructField("IncidentNumber", IntegerType, nullable = true),
          StructField("CallType", StringType, nullable = true),
          StructField("CallDate", StringType, nullable = true),
          StructField("WatchDate", StringType, nullable = true),
          StructField("CallFinalDisposition", StringType, nullable = true),
          StructField("AvailableDtTm", StringType, nullable = true),
          StructField("Address", StringType, nullable = true),
          StructField("City", StringType, nullable = true),
          StructField("Zipcode", IntegerType, nullable = true),
          StructField("Battalion", StringType, nullable = true),
          StructField("StationArea", StringType, nullable = true),
          StructField("Box", StringType, nullable = true),
          StructField("OriginalPriority", StringType, nullable = true),
          StructField("Priority", StringType, nullable = true),
          StructField("FinalPriority", IntegerType, nullable = true),
          StructField("ALSUnit", BooleanType, nullable = true),
          StructField("CallTypeGroup", StringType, nullable = true),
          StructField("NumAlarms", IntegerType, nullable = true),
          StructField("UnitType", StringType, nullable = true),
          StructField("UnitSequenceInCallDispatch", IntegerType, nullable = true),
          StructField("FirePreventionDistrict", StringType, nullable = true),
          StructField("SupervisorDistrict", StringType, nullable = true),
          StructField("Neighborhood", StringType, nullable = true),
          StructField("Location", StringType, nullable = true),
          StructField("RowID", StringType, nullable = true),
          StructField("Delay", FloatType, nullable = true)
        )
      )

    // Read the file using the CSV DataFrameReader
    val sfFireFile = "databricks-datasets/learning-spark-v2/sf-fire/sf-fire-calls.csv"

    val spark: SparkSession =
      SparkSession.builder.appName("dataframe").config("spark.master", "local").getOrCreate

    val fireDF = spark.read.schema(fileSchema)
      .option("header", "true")
      .csv(sfFireFile)
  }
}