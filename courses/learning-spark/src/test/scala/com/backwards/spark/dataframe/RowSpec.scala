package com.backwards.spark.dataframe

import org.apache.spark.sql.{Row, SparkSession}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class RowSpec extends AnyFreeSpec with Matchers {
  "Create row" - {
    val blogRow = Row(6, "Reynold", "Xin", "https://tinyurl.6", 255568, "3/2/2015", Array("twitter", "LinkedIn"))

    // Access using index for individual items
    println(blogRow(1))
  }

  "Row objects can be used to create DataFrames if you need them for quick interactivity and exploration" - {
    val spark: SparkSession =
      SparkSession.builder.appName("dataframe").config("spark.master", "local").getOrCreate

    import spark.sqlContext.implicits._

    val rows = Seq(("Matei Zaharia", "CA"), ("Reynold Xin", "CA"))
    val authorsDF = rows.toDF("Author", "State")
    authorsDF.show()
  }
}