# Resilient Distributed Datasets (RDD)

RDD is a fundamental data structure of Spark.
It is an immutable distributed collection of objects.
Each dataset in RDD is divided into logical partitions, which may be computed on different nodes of the cluster.

Take a look at [RDDParalleizeApp](../src/main/scala/com/backwards/spark/RDDParallelizeApp.scala) which has:

```scala
val spark: SparkSession = SparkSession.builder()
.master("local[3]")
.appName("SparkByExamples.com")
.getOrCreate()

val rdd: RDD[Int] = spark.sparkContext.parallelize(List(1, 2, 3, 4, 5))

val rddCollect: Array[Int] = rdd.collect()

println(s"Number of Partitions: ${rdd.getNumPartitions}")
println(s"Action: First element: ${rdd.first()}")
println("Action: RDD converted to Array[Int]: ")
rddCollect foreach println
```

producing:
```scala
Number of Partitions: 3
Action: First element: 1
Action: RDD converted to Array[Int]: 
1
2
3
4
5
```

## Read Text File

- **textFile** - Reads single or multiple files giving single Spark RDD[String]
- **wholeTestFiles** - Reads single or multiple files giving single Spark RDD[(String, String)] of **file name** / **file content** pair

**textFile** and **wholeTextFile** returns an error when it finds a nested folder hence, first create a file path list by traversing all nested folders and pass all file names with comma separator in order to create a single RDD.

You can also read all text files into a separate RDD’s and union all these to create a single RDD.