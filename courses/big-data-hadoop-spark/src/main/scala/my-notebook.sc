// Databricks notebook source
// MAGIC %sh
// MAGIC wget https://raw.githubusercontent.com/backwards-limited/spark-backwards/master/courses/big-data-hadoop-spark/src/main/resources/retailstore.csv

// COMMAND ----------

// MAGIC %sh
// MAGIC pwd

// COMMAND ----------

val customerDF = spark.read.csv("/FileStore/tables/retailstore.csv")

// COMMAND ----------

customerDF.show()

// COMMAND ----------

val customerDF = spark.read.option("header", "true").csv("/FileStore/tables/retailstore.csv")

// COMMAND ----------

customerDF.show()

// COMMAND ----------

customerDF.head()

// COMMAND ----------

customerDF.show(5)

// COMMAND ----------

customerDF.groupBy("Gender").count()

// COMMAND ----------

customerDF.groupBy("Gender").count().show()

// COMMAND ----------

customerDF.describe().show()

// COMMAND ----------

customerDF.select("Country").show()

// COMMAND ----------

customerDF.groupBy("Country").count().show()

// COMMAND ----------

customerDF.createOrReplaceTempView("customer")

// COMMAND ----------

val results = spark.sql("select * from customer")

// COMMAND ----------

val results2 = spark.sql("select * from customer where age > 22")

// COMMAND ----------

results2.show()

// COMMAND ----------

customerDF.select("age", "salary").show()

// COMMAND ----------

customerDF.filter("Salary > 30000").select("Age").show()

// COMMAND ----------

customerDF.printSchema()

// COMMAND ----------

val customerDF2 = spark.read.option("header", "true").option("inferSchema", "true").csv("/FileStore/tables/retailstore.csv")

// COMMAND ----------

customerDF2.printSchema()

// COMMAND ----------

import org.apache.spark.sql.functions._

// COMMAND ----------

customerDF2.select(countDistinct("country")).show()

// COMMAND ----------

customerDF2.select(avg("salary")).show()

// COMMAND ----------

customerDF2.select(countDistinct("country").alias("Distinct Countries")).show()

// COMMAND ----------

customerDF2.orderBy("salary").show()

// COMMAND ----------

customerDF2.na.drop().show()

// COMMAND ----------

customerDF2.na.fill(0).show()

// COMMAND ----------

customerDF2.select($"Country").show()

// COMMAND ----------

customerDF2.filter($"Salary" > 30000).select("Age").show()

// COMMAND ----------

import spark.implicits._

// COMMAND ----------


