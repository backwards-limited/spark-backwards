# Resilient Distributed Dataset

The following illustrates a **RDD** of numbers (**1** to **18**) having nine partitions on a cluster of three nodes:

![RDD](images/rdd.png)

An RDD can be created in four ways (where we need a SparkSession):

```scala
val spark = SparkSession
  .builder
  .appName("test")
  .config("spark.master", "local")
  .getOrCreate()
```

- Parallelize a collection

```scala
val numbersRDD = spark.sparkContext.parallelize(1 to 10)
```

- From an external dataset

  Though parallelizing a collection is the easiest way to create an RDD, it is not the recommended way for the large datasets. Large datasets are generally stored on filesystems such as HDFS, and we know that Spark is built to process big data.

```scala
val filePath = File("./data/input/employees.txt").canonicalPath
val rdd = spark.sparkContext.textFile(filePath)
```

- From another RDD

  RDDs are immutable in nature. They cannot be modified, but we can transform an RDD to another RDD with the help of the methods provided by Spark.

```scala
val oddNumbersRDD = numbersRDD.filter(_ % 2 != 0)
```

- From a DataFrame or DataSet

```scala
val rangeDataFrame = spark.range(1, 5)
val rangeRDD = rangeDataFrame.rdd
```

