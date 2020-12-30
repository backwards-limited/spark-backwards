package com.backwards.spark

import cats.data.Kleisli
import cats.effect.IO
import cats.implicits._
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import com.backwards.spark.Spark._

/**
 * Build JAR:
 * {{{
 *  sbt -J-Xms2048m -J-Xmx2048m -J-DmainClass=com.backwards.spark.SparkSubmitExample big-data-hadoop-spark/clean big-data-hadoop-spark/assembly
 * }}}
 *
 * Boot our Cloudera Docker image:
 * {{{
 *  docker run --name cloudera --hostname=quickstart.cloudera --privileged=true -it -p 7180:7180 -p 8888:8888 davidainslie/cloudera /usr/bin/docker-quickstart
 * }}}
 *
 * Copy the generated JAR into the running container (e.g. from root project):
 * {{{
 *  docker cp courses/big-data-hadoop-spark/target/scala-2.12/big-data-hadoop-spark.jar cloudera:/
 * }}}
 *
 * Submit JAR
 * {{{
 *  cd /root
 *  export HADOOP_CONF_DIR=/usr/lib/hadoop
 *  export YARN_CONF_DIR=/usr/lib/hadoop
 *  spark-submit --class com.backwards.spark.SparkSubmitExample --master yarn --deploy-mode client big-data-hadoop-spark.jar
 * }}}
 */
object SparkSubmitExample {
  def main(args: Array[String]): Unit =
    program.unsafeRunSync()

  def program: IO[Unit] =
    sparkSession(_.appName("spark-submit-example")).use { spark =>
      val program =
        for {
          courses <- courses
          _ = courses.show()
        } yield ()

      program run spark
    }

  def courses: Kleisli[IO, SparkSession, Dataset[Row]] =
    Kleisli(spark => IO delay
      spark.createDataFrame(List(1 -> "Spark", 2 -> "Big Data")).toDF("course id", "course name")
    )
}