package com.backwards.spark

import better.files.Resource
import cats.data.ReaderT
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import com.backwards.spark.Spark.sparkSession

object StructuredStreamingExample {
  def main(array: Array[String]): Unit =
    sparkSession(_.appName("structured-streaming-example").master("local[*]"))
      .use(program.run)
      .unsafeRunSync()

  def program: ReaderT[IO, SparkSession, Unit] =
    ReaderT(readEmployees).flatMapF(writeEmployees).map(_.awaitTermination())

  def readEmployees(spark: SparkSession): IO[Dataset[Row]] =
    IO(StructType(Array(StructField("empId", StringType), StructField("empName", StringType)))).map(schema =>
      spark.readStream.option("header","true").schema(schema).csv(Resource.getUrl("employees").getPath)
    )

  def writeEmployees(employees: Dataset[Row]): IO[StreamingQuery] =
    IO(employees.writeStream.format("console").outputMode(OutputMode.Update()).start())
}