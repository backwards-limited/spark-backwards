package com.backwards.spark.aws

import java.io.File
import scala.util.chaining.scalaUtilChainingOps
import cats.data.Kleisli
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import org.apache.hadoop.fs.s3a.S3AFileSystem
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import org.apache.spark.sql.{Dataset, Row, SaveMode, SparkSession}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.testcontainers.containers.localstack.LocalStackContainer.Service
import com.amazonaws.services.s3.model._
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.dimafeng.testcontainers.{ForAllTestContainer, LocalStackContainer}
import com.backwards.spark.Spark._

/**
 * [[https://medium.com/@sumitsu/unit-testing-aws-s3-integrated-scala-spark-components-using-local-s3-mocking-tools-8bb90fd58fa2 Unit-testing AWS S3-integrated Scala / Spark components using local S3 mocking tools]]
 *
 * Hadoop configuration example:
 * {{{
 *  .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
 *  .config("spark.hadoop.fs.s3a.access.key", "my access key")
 *  .config("spark.hadoop.fs.s3a.secret.key", "my secret key")
 *  .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
 *  .config("spark.hadoop.fs.s3a.multiobjectdelete.enable","false")
 *  .config("spark.hadoop.fs.s3a.fast.upload","true")
 *  .config("spark.sql.parquet.filterPushdown", "true")
 *  .config("spark.sql.parquet.mergeSchema", "false")
 *  .config("spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version", "2")
 *  .config("spark.speculation", "false")
 * }}}
 */
class S3Spec extends AnyWordSpec with Matchers with ForAllTestContainer {
  override val container: LocalStackContainer =
    LocalStackContainer(services = List(Service.S3))

  def createBucket(name: String): Kleisli[IO, AmazonS3, Bucket] =
    Kleisli(s3 =>
      IO(s3.createBucket(new CreateBucketRequest(name)))
    )

  def write(bucketName: String, file: File): Kleisli[IO, AmazonS3, PutObjectResult] =
    Kleisli(s3 =>
      IO delay s3.putObject(new PutObjectRequest(bucketName, file.getName, file))
    )

  // TODO - Resource.make resulting in a Resource instead of S3Object
  // TODO - Refine types
  def read(bucketName: String, key: String, versionId: String): Kleisli[IO, AmazonS3, S3Object] =
    Kleisli(s3 =>
      IO delay s3.getObject(new GetObjectRequest(bucketName, key).withVersionId(versionId))
    )

  def write(path: String): Kleisli[IO, SparkSession, Dataset[Row]] =
    Kleisli { spark =>
      import spark.implicits._

      println(s"===> WRITE path: $path")

      IO(spark.createDataset(spark.sparkContext.parallelize(0 until 500)).toDF("number").tap(_.write.mode(SaveMode.Overwrite).json(path)))
    }

  def read(path: String): Kleisli[IO, SparkSession, Dataset[Row]] =
    Kleisli { spark =>
      println(s"===> READ path: $path")

      IO(spark.read.schema(StructType(List(StructField(name = "number", dataType = IntegerType)))).json(path))
    }

  "Spark with S3" should {
    "write and read" in {
      val s3: AmazonS3 = {
        println(s"S3 default port: 4572 -> host port: ${container.mappedPort(4572)}")
        println(s"S3 endpoint: ${container.endpointConfiguration(Service.S3).getServiceEndpoint}")

        AmazonS3ClientBuilder
          .standard
          .withPathStyleAccessEnabled(true)
          .withEndpointConfiguration(container.endpointConfiguration(Service.S3))
          .withCredentials(container.defaultCredentialsProvider)
          .disableChunkedEncoding
          .build
      }

      val sparkBuilder: SparkSession.Builder => SparkSession.Builder =
        _.appName("test")
          .master("local")
          // Indicates the FileSystem implementation to use for S3
          .config("spark.hadoop.fs.s3a.impl", classOf[S3AFileSystem].getName)
          // Indicates the URI of the local mock S3
          .config("spark.hadoop.fs.s3a.endpoint", container.endpointConfiguration(Service.S3).getServiceEndpoint)
          // Even though the mock S3 server does not require access credentials, it seems that the S3A layer currently performs a non-empty check
          .config("spark.hadoop.fs.s3a.access.key", "my access key")
          .config("spark.hadoop.fs.s3a.secret.key", "my secret key")
          .config("spark.hadoop.fs.s3a.attempts.maximum", "3")
          .config("spark.hadoop.fs.s3a.path.style.access", "true")
          .config("spark.hadoop.fs.s3a.multiobjectdelete.enable", "false")
          .config("spark.hadoop.fs.s3a.change.detection.version.required", "false")
          .config("spark.hadoop.fs.s3a.fast.upload.buffer", "bytebuffer")
          .config("spark.hadoop.fs.s3a.fast.upload", "true")
          // Unlike the AmazonS3 client, the S3A client does not offer an option to disable chunked encoding (as is available via the .disableChunkedEncoding method when building AmazonS3 directly).
          // S3A uses an S3ClientFactory in order to generate the internal AmazonS3 instance needed to communicate with the S3 endpoint.
          // The default implementation is DefaultS3ClientFactory - extend this and override createS3Client in order to apply the additional .disableChunkedEncoding option.
          .config("spark.hadoop.fs.s3a.s3.client.factory.impl", classOf[NonChunkedDefaultS3ClientFactory].getName)

      def process(s3: AmazonS3)(spark: SparkSession): IO[Dataset[Row]] = for {
        bucket <- createBucket("my-bucket") run s3
        r <- write(s"s3a://${bucket.getName}/blah") run spark
        // x <- read(s"s3a://${bucket.getName}/blah") run spark TODO - ETag issue
        _ = r.show(10)
      } yield r

      val program: IO[Dataset[Row]] =
        sparkResource(sparkBuilder).use(process(s3))

      program.unsafeRunSync()

      // TODO - WIP
      succeed
    }
  }
}