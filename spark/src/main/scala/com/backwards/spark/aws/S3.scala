package com.backwards.spark.aws

import java.io.File
import java.net.URI
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.model.{Bucket, PutObjectResult}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import cats.data.Kleisli
import cats.effect.{IO, Resource}
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, AwsCredentialsProvider, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.{S3Client, S3ClientBuilder, S3Configuration}
import com.amazonaws.services.s3.transfer.MultipleFileUpload
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.transfer.TransferManagerBuilder

// TODO - Monadic logging
object S3 {
  def s3Client(
    awsCredentialsProvider: => AwsCredentialsProvider,
    region: => Region,
    endpoint: => URI,
    s3ClientBuilder: S3ClientBuilder => S3ClientBuilder = identity
  ): Resource[IO, S3Client] = {
    val aquire: IO[S3Client] =
      IO(println("Aquiring AWS S3 Client")) >> IO {
        println(s"AWS S3 Client endpoint: $endpoint")

        s3ClientBuilder(S3Client
          .builder()
          .serviceConfiguration(S3Configuration.builder()
            .checksumValidationEnabled(false)
            .chunkedEncodingEnabled(false)
            .pathStyleAccessEnabled(true)
            .build()
          )
          .credentialsProvider(awsCredentialsProvider)
          .region(region)
          .endpointOverride(endpoint)
        ).build()
      }

    val release: S3Client => IO[Unit] =
      s3Client => IO(println("Shutting down AWS S3 Client")).as(s3Client.close())

    Resource.make(aquire)(release)
  }

  @deprecated
  def s3(
    awsEndpointConfiguration: => AwsClientBuilder.EndpointConfiguration,
    awsCredentialsProvider: => AWSCredentialsProvider,
    f: AmazonS3ClientBuilder => AmazonS3ClientBuilder = identity
  ): Resource[IO, AmazonS3] = {
    val aquire: IO[AmazonS3] =
      IO(println("Aquiring AWS S3 client")) >> IO(
        f(AmazonS3ClientBuilder
          .standard()
          .withPathStyleAccessEnabled(true)
          .withEndpointConfiguration(awsEndpointConfiguration)
          .withCredentials(awsCredentialsProvider)
          .disableChunkedEncoding()
        ).build()
      )

    val release: AmazonS3 => IO[Unit] =
      s3 => IO(println("Shutting down AWS S3 client")).as(s3.shutdown())

    Resource.make(aquire)(release)
  }

  def createBucket(name: String): Kleisli[IO, AmazonS3, Bucket] =
    Kleisli(s3 =>
      IO(s3 createBucket name)
    )

  def putObject(bucketName: String, key: String, content: String): Kleisli[IO, AmazonS3, PutObjectResult] =
    Kleisli(s3 =>
      IO(s3.putObject(bucketName, key, content))
    )

  def putObject(bucketName: String, key: String, file: File): Kleisli[IO, AmazonS3, PutObjectResult] =
    if (file.isDirectory) Kleisli { s3 =>
      IO {
        val transferManager: TransferManager =
          TransferManagerBuilder.standard.withS3Client(s3).build

        val multipleFileUpload: MultipleFileUpload =
          transferManager.uploadDirectory(bucketName, key, file, true)

        multipleFileUpload.waitForCompletion()

        // TODO - Obviously this result actually has no information
        new PutObjectResult
      }
    } else Kleisli { s3 =>
      IO(s3.putObject(bucketName, key, file))
    }
}