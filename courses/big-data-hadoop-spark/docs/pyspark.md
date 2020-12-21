# Pyspark

## Python

![Python list](images/python-list.png)

When working in Google Colab, we will want to upload and run [install-spark-testing-pyspark](../src/main/python/install-spark-testing-pyspark.ipynb) and then add to it and save with a different name, since this code configures our required Spark environment.

## Lambda
- An anonymous function
- E.g. x = lambda a, b: a + b

## RDD
- **Transformation** produces new RDD from an existing RDD (lazy and immutable)
- **Action** returns the final result of RDD computation (collect, count, take etc.)

## Dataframe

Spark Dataframe is like an in-memory excel.
It holds the data in row and column format and provides various methods to analyse and apply transformation on the data.
Dataframes can be constructed from a wide range of sources such as: structured data; files; tables in Hive; external databases; existing RDDs.

The **sql** function on a SparkSession enables applications to run SQL queries programmatically and return the result as a DataFrame.