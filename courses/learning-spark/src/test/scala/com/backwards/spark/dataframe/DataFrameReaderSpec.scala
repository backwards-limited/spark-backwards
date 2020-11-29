package com.backwards.spark.dataframe

import java.util.UUID
import scala.language.postfixOps
import better.files.Resource
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.types._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.apache.spark.sql.functions._

class DataFrameReaderSpec extends AnyFreeSpec with Matchers {
  val spark: SparkSession =
    SparkSession.builder.appName("dataframe").config("spark.master", "local").getOrCreate

  import spark.implicits._

  "DataFrameReader" - {
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
    val path: String = Resource.getUrl("sf-fire-calls.csv").getFile

    val df = spark.read
      .schema(fileSchema)
      .option("header", "true")
      .csv(path)

    // Save as Parquet file
    df.write.format("parquet").save(path.substring(0, path.lastIndexOf("/")) + "/" + UUID.randomUUID() + "/" + "sf-fire-results.parquet")

    val fewDF = df
      .select("IncidentNumber", "AvailableDtTm", "CallType")
      .where($"CallType" =!= "Medical Incident")

    fewDF.show(5, truncate = false)

    // How many distinct CallTypes were recorded as the causes of the fire calls?
    df
      .select("CallType")
      .where(col("CallType") isNotNull)
      .agg(countDistinct('CallType) as 'DistinctCallTypes)
      .show()

    // We can list the distinct call types in the data set using these queries:
    df
      .select("CallType")
      .where($"CallType" isNotNull)
      .distinct
      .show(10, truncate = false)

    // By specifying the desired column names in the schema with StructField, as we did, we effectively changed all names in the resulting DataFrame.
    // Alternatively, you could selectively rename columns with the withColumnRenamed()
    df
      .withColumnRenamed("Delay", "ResponseDelayedinMins")
      .select("ResponseDelayedinMins")
      .where($"ResponseDelayedinMins" > 5)
      .show(5, truncate = false)

    // Changing types of columns by replacing with new ones:
    val datesDF: DataFrame = df
      .withColumn("IncidentDate", to_timestamp(col("CallDate"), "MM/dd/yyyy")).drop("CallDate")
      .withColumn("OnWatchDate", to_timestamp(col("WatchDate"), "MM/dd/yyyy")).drop("WatchDate")
      .withColumn("AvailableDtTS", to_timestamp(col("AvailableDtTm"), "MM/dd/yyyy hh:mm:ss a")).drop("AvailableDtTm")

    datesDF
      .select("IncidentDate", "OnWatchDate", "AvailableDtTS")
      .show(5, truncate = false)

    datesDF
      .select(year($"IncidentDate"))
      .distinct
      .orderBy(year($"IncidentDate"))
      .show

    // Aggregations - What were the most common types of fire calls?
    df
      .select("CallType")
      .where(col("CallType").isNotNull)
      .groupBy("CallType")
      .count()
      .orderBy(desc("count"))
      .show(10, truncate = false)

    // sum, avg, min, max
    df
      .select(sum("NumAlarms"), avg("Delay"), min("Delay"), max("Delay"))
      .show()
  }
}