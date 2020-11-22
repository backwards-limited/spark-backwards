package com.backwards.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}
import com.amazonaws.auth.profile.ProfileCredentialsProvider

object S3App extends App {
  val spark: SparkSession = SparkSession.builder()
    .master("local[1]")
    .appName("s3-demo")
    .getOrCreate()

  val awsCredentials = new ProfileCredentialsProvider("david").getCredentials

  spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint", "s3.amazonaws.com")
  spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", awsCredentials.getAWSAccessKeyId)
  spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", awsCredentials.getAWSSecretKey)

  val rddFromFile = spark.sparkContext.textFile("s3a://csv-backwards/text01.csv")
  println(rddFromFile.getClass)

  println("===> Get data using collect")
  rddFromFile.collect().foreach(println)

  // wholeTextFiles() reads a text file into PairedRDD of type RDD[(String, String)] with the key being the file path and value being contents of the file.
  println("===> Read whole text files")
  val rddWhole = spark.sparkContext.wholeTextFiles("s3a://csv-backwards/text01.csv")

  println("===> Get whole data using collect")
  rddWhole.collect().foreach(println)

  println("===> Read all files from a directory to single RDD")
  val rdd2 = spark.sparkContext.textFile("s3a://csv-backwards/*")
  rdd2.foreach(println)

  // BE CAREFUL
  // textFile() and wholeTextFile() returns an error when it finds a nested folder.
  // You could create a file path list by traversing all nested folders and pass all file names with comma separator in order to create a single RDD.

  // Using spark.read.text() and spark.read.textFile() We can read a single text file, multiple files and all files from a directory on S3 bucket into Spark DataFrame and Dataset.
  // Note: These methods don’t take an argument to specify the number of partitions.

  // Returns DataFrame
  val df: DataFrame = spark.read.text("s3a://csv-backwards/text01.csv")
  df.printSchema()
  df.show(false)

  // Each line in a text file represents a record in DataFrame with just one column “value”.
  // In case if you want to convert into multiple columns, you can use map transformation and split method to transform,
}