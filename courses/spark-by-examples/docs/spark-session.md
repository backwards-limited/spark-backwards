# Spark Session

Since Spark 2.0, SparkSession has become an entry point to Spark to work with RDD, DataFrame, and Dataset.
Prior to 2.0, SparkContext used to be an entry point.

With Spark 2.0 a new class **org.apache.spark.sql.SparkSession** has been introduced to use which is a combined class for all different contexts we used to have prior to 2.0 e.g. SQLContext and HiveContext etc.
SparkSession internally creates SparkConfig and SparkContext with the configuration provided with SparkSession.

In the Spark REPL:
```bash
➜ spark-shell

scala> val sc = spark.sqlContext
sc: org.apache.spark.sql.SQLContext = org.apache.spark.sql.SQLContext@43c16e17
```

Programmatically:
```scala
val spark = SparkSession.builder()
  .master("local[1]")
  .appName("SparkByExamples.com")
  .getOrCreate()
```

**master()** – If you are running it on the cluster you need to use your master name as an argument to master().
Usually, it would be either **yarn** or **mesos** depends on your cluster setup.

Use **local[x]** when running in Standalone mode.
x should be an integer value and should be greater than 0; this represents how many partitions it should create when using RDD, DataFrame, and Dataset.
Ideally, x value should be the number of CPU cores you have.

**appName()** – Used to set your application name.

**getOrCreate()** – This returns a SparkSession object if already exists, creates new one if not exists.