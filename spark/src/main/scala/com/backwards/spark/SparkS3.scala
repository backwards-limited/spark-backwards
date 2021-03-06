package com.backwards.spark

import scala.language.higherKinds
import scala.util.chaining.scalaUtilChainingOps
import cats.effect.{IO, Resource}
import cats.implicits._
import org.apache.hadoop.fs.s3a.S3AFileSystem
import org.apache.spark.sql.SparkSession
import com.amazonaws.client.builder.AwsClientBuilder
import com.backwards.spark.aws.NonChunkedDefaultS3ClientFactory

// TODO - WRONG - Split out
// TODO - Monadic logging
object SparkS3 {
  /**
   * Allow for both s3 and s3a protocols: [[https://docs.qubole.com/en/latest/user-guide/cloud-filesystem/aws-filesystem.html Using the AWS File System]]
   */
  def sparkSession(
    f: SparkSession.Builder => SparkSession.Builder = identity,
    awsEndpointConfiguration: => Option[AwsClientBuilder.EndpointConfiguration] = None
  ): Resource[IO, SparkSession] = {
    val aquire: IO[SparkSession] =
      IO(println("Aquiring Spark Session")) >> IO(
        f(SparkSession.builder).pipe(sb =>
          awsEndpointConfiguration.map(endpointConfig =>
          sb.config("spark.hadoop.fs.s3.impl", classOf[S3AFileSystem].getName)
            .config("spark.hadoop.fs.s3a.impl", classOf[S3AFileSystem].getName)
            .config("spark.hadoop.fs.s3a.endpoint", endpointConfig.getServiceEndpoint)
            .config("spark.hadoop.fs.s3.access.key", "ignored-access-key")
            .config("spark.hadoop.fs.s3a.access.key", "ignored-access-key")
            .config("spark.hadoop.fs.s3.secret.key", "ignored-secret-key")
            .config("spark.hadoop.fs.s3a.secret.key", "ignored-secret-key")
            .config("spark.hadoop.fs.s3a.attempts.maximum", "3")
            .config("spark.hadoop.fs.s3a.path.style.access", "true")
            .config("spark.hadoop.fs.s3a.multiobjectdelete.enable", "false")
            .config("spark.hadoop.fs.s3a.change.detection.version.required", "false")
            .config("spark.hadoop.fs.s3a.fast.upload", "true")
            .config("spark.hadoop.fs.s3a.fast.upload.buffer", "bytebuffer")
            .config("spark.hadoop.fs.s3a.s3.client.factory.impl", classOf[NonChunkedDefaultS3ClientFactory].getName)
            /*.config("spark.hadoop.fs.s3a.signing-algorithm", "AWS3SignerType")
            .config("spark.hadoop.fs.s3a.connection.ssl.enabled", "false")
            .config("spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version", "2")
            .config("spark.sql.parquet.filterPushdown", "true")
            .config("spark.sql.parquet.mergeSchema", "false")
            .config("spark.speculation", "false")*/
          ).getOrElse(sb)
        ).getOrCreate
      )

    val release: SparkSession => IO[Unit] =
      spark => IO(println("Closing Spark Session")) >> IO(spark.close())

    Resource.make(aquire)(release)
  }
}