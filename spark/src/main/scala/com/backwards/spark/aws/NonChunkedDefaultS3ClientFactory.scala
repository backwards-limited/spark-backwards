package com.backwards.spark.aws

import java.io.IOException
import java.net.URI
import org.apache.hadoop.fs.s3a.{DefaultS3ClientFactory, S3ClientFactory}
import com.amazonaws.services.s3.{AmazonS3, S3ClientOptions}

class NonChunkedDefaultS3ClientFactory extends DefaultS3ClientFactory {
  @throws[IOException]
  override def createS3Client(uri: URI, parameters: S3ClientFactory.S3ClientCreationParameters): AmazonS3 = {
    val s3: AmazonS3 = super.createS3Client(uri, parameters)

    s3.setS3ClientOptions(S3ClientOptions.builder.disableChunkedEncoding.setPathStyleAccess(true).build)
    s3
  }
}