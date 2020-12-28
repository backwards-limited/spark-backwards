// Databricks notebook source
val bankProspectsDF = spark.read.option("header", "true").option("inferSchema", "true").csv("/FileStore/tables/bank_prospects.csv")

// COMMAND ----------

bankProspectsDF.show()

// COMMAND ----------

val bankProspectsDF1 = bankProspectsDF.filter($"Country" !== "unknown")

// COMMAND ----------

bankProspectsDF1.show()

// COMMAND ----------

import org.apache.spark.sql.functions._

// COMMAND ----------

val meanAgeArray = bankProspectsDF1.select(avg($"age")).collect()

// COMMAND ----------

meanAgeArray.head

// COMMAND ----------

val meanAge = meanAgeArray(0)(0).toString.toDouble

// COMMAND ----------

val meanSalaryArray = bankProspectsDF1.select(avg("salary")).collect()

// COMMAND ----------

meanSalaryArray.head

// COMMAND ----------

val meanSalary = meanSalaryArray.head(0).toString.toDouble

// COMMAND ----------

val bankProspectsDF2 = bankProspectsDF1.na.fill(meanAge, Array("Age"))

// COMMAND ----------

bankProspectsDF2.show()

// COMMAND ----------

val bankProspectsDF3 = bankProspectsDF2.na.fill(meanSalary, Array("Salary"))

// COMMAND ----------

bankProspectsDF3.show()

// COMMAND ----------

bankProspectsDF3.write.format("csv").save("bank-prospects-transformed")

// COMMAND ----------


