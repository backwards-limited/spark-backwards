package com.backwards.spark

import java.net.URL
import better.files.Resource.{getUrl => resourceUrl}
import cats.data.Kleisli
import cats.effect.IO
import cats.implicits._
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.sql.functions._
import com.backwards.spark.Spark._

object _5a {
  def main(args: Array[String]): Unit =
    program.unsafeRunSync()

  def program: IO[Unit] =
    sparkSession(_.appName("5a").master("local")).use { spark =>
      val program: Kleisli[IO, SparkSession, Unit] = {
        for {
          customers <- load(resourceUrl("customers.csv"))
          products <- load(resourceUrl("products.csv"))
          purchases <- load(resourceUrl("purchases.csv"))
        } yield
          customers
            .join(purchases, customers.col("customer_id").equalTo(purchases.col("customer_id")))
            .join(products, purchases.col("product_id").equalTo(products.col("product_id")))
            .drop("favorite_website")
            .drop(purchases.col("customer_id"))
            .drop(purchases.col("product_id"))
            .groupBy("first_name", "product_name")
            .agg(
              count("product_name").as("number_of_purchases"),
              max("product_price").as("most_exp_purchase"),
              sum("product_price").as("total_spent")
            )
            .drop("number_of_purchases")
            .drop("most_exp_purchase")
            .show()
      }

      program run spark
    }

  def load(url: URL): Kleisli[IO, SparkSession, Dataset[Row]] =
    Kleisli { spark =>
      IO delay
        spark.read.format("csv")
          .option("inferschema", value = "true")
          .option("header", value = true)
          .load(url.getFile)
    }
}
