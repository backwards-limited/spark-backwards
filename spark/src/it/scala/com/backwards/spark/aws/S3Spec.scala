package com.backwards.spark.aws

import java.io.File
import scala.util.chaining.scalaUtilChainingOps
import cats.data.Kleisli
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.model._
import software.amazon.awssdk.services.s3.{S3Client, S3Configuration}
import org.apache.hadoop.fs.s3a.S3AFileSystem
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import org.apache.spark.sql.{Dataset, Row, SaveMode, SparkSession}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.testcontainers.containers.localstack.LocalStackContainer.Service
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

  def createBucket(name: String): Kleisli[IO, S3Client, CreateBucketResponse] =
    Kleisli(s3 =>
      IO(s3.createBucket(CreateBucketRequest.builder().bucket(name).build()))
    )

  def write(bucketName: String, file: File): Kleisli[IO, S3Client, PutObjectResponse] =
    Kleisli(s3 =>
      IO delay s3.putObject(PutObjectRequest.builder().bucket(bucketName).key(file.getName).build(), RequestBody.fromFile(file))
    )

  // TODO - Resource.make resulting in a Resource instead of S3Object
  // TODO - Refine types
  def read(bucketName: String, key: String, versionId: String): Kleisli[IO, S3Client, ResponseInputStream[GetObjectResponse]] =
    Kleisli(s3 =>
      IO delay s3.getObject(GetObjectRequest.builder().bucket(bucketName).key(key).versionId(versionId).build())
    )

  def write(path: String): Kleisli[IO, SparkSession, Dataset[Row]] =
    Kleisli { spark =>
      import spark.implicits._

      println(s"===> WRITE path: $path")

      IO(
        spark.createDataset(spark.sparkContext.parallelize(0 until 500)).toDF("number")
          .tap(_.write.mode(SaveMode.Overwrite).option("fs.s3a.committer.name", "partitioned").option("fs.s3a.committer.staging.conflict-mode", "replace").json(path)))
    }

  def read(path: String): Kleisli[IO, SparkSession, Dataset[Row]] =
    Kleisli { spark =>
      println(s"===> READ path: $path")

      IO(spark.read.schema(StructType(List(StructField(name = "number", dataType = IntegerType)))).json(path))
    }

  "Spark with S3" should {
    "write and read" in {
      val s3: S3Client =
        S3Client
          .builder()
          .serviceConfiguration(S3Configuration.builder()
            .checksumValidationEnabled(false)
            .chunkedEncodingEnabled(false)
            .pathStyleAccessEnabled(true)
            .build()
          )
          .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(container.container.getAccessKey, container.container.getSecretKey)))
          .region(Region.of(container.container.getRegion))
          .endpointOverride(container.container.getEndpointOverride(Service.S3))
          .build()

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

      def process(s3: S3Client)(spark: SparkSession): IO[Dataset[Row]] =
        for {
          bucket  <- IO(Bucket.builder().name("my-bucket").build())
          _       <- createBucket(bucket.name()) run s3
          r       <- write(s"s3a://${bucket.name()}/blah") run spark
          // x <- read(s"s3a://${bucket.name()/blah") run spark //TODO - ETag issue
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