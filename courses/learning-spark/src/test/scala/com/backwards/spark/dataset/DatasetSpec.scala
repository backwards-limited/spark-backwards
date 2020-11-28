package com.backwards.spark.dataset

import java.util.UUID
import scala.language.postfixOps
import better.files.Resource
import io.circe.literal.JsonStringContext
import io.scalaland.chimney.dsl._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class DatasetSpec extends AnyFreeSpec with Matchers {
  val spark: SparkSession =
    SparkSession.builder.appName("dataset").config("spark.master", "local").getOrCreate

  import spark.implicits._

  "Dateset" - {
    val row: Row = Row(350, true, "Learning Spark 2E", null)

    val intField: Int = row.getInt(0)
    val booleanField: Boolean = row.getBoolean(1)
    val stringField: String = row.getString(2)

    /*
    As with creating DataFrames from data sources, when creating a Dataset you have to know the schema.
    In other words, you need to know the data types.
    Although with JSON and CSV data it’s possible to infer the schema, for large data sets this is resource-intensive (expensive).
    When creating a Dataset in Scala, the easiest way to specify the schema for the resulting Dataset is to use an ADT.
    */

    val iotJson = json"""{
      "device_id": 198164,
      "device_name": "sensor-pad-198164owomcJZ",
      "ip": "80.55.20.25",
      "cca2": "PL",
      "cca3": "POL",
      "cn": "Poland",
      "latitude": 53.080000,
      "longitude": 18.620000,
      "scale": "Celsius",
      "temp": 21,
      "humidity": 65,
      "battery_level": 8,
      "c02_level": 1408,
      "lcd": "red",
      "timestamp" :1458081226051
    }"""

    val path = Resource.getUrl("iot-devices.json").getFile

    val ds: Dataset[DeviceIoTData] = spark.read
      .json(path)
      .as[DeviceIoTData]

    ds.show(5, truncate = false)

    // Transformations nicer than Dataframes
    ds
      .filter(d => d.temp > 30 && d.humidity > 70)
      .show(5, truncate = false)

    ds
      .filter(_.temp > 25)
      .map(d => (d.temp, d.device_name, d.device_id, d.cca3))
      // Without this you get: cannot resolve '`temp`' given input columns: [_1, _2, _3, _4]
      .toDF("temp", "device_name", "device_id", "cca3")
      .as[DeviceTempByCountry]
      .show(5, truncate = false)

    // Avoiding toDF
    ds
      .filter(_.temp > 25)
      .map(d => (d.temp, d.device_name, d.device_id, d.cca3))
      .withColumnRenamed("_1", "temp")
      .withColumnRenamed("_2", "device_name")
      .withColumnRenamed("_3", "device_id")
      .withColumnRenamed("_4", "cca3")
      .as[DeviceTempByCountry]
      .show(5, truncate = false)

    // OR with a bit of chimney - later I'll try to put together a Shapeless version
    ds
      .filter(_.temp > 25)
      .map(d => d.transformInto[DeviceTempByCountry])
      .show(5, truncate = false)
  }
}

final case class DeviceIoTData(
  battery_level: Long, c02_level: Long,
  cca2: String, cca3: String, cn: String, device_id: Long,
  device_name: String, humidity: Long, ip: String, latitude: Double,
  lcd: String, longitude: Double, scale:String, temp: Long,
  timestamp: Long
)

final case class DeviceTempByCountry(temp: Long, device_name: String, device_id: Long, cca3: String)