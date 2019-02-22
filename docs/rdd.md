# Resilient Distributed Datasets (RDD)

- What is a dataset?

  - Essentially it's a collection of data e.g. list of strings, rows in a relational database.

- RDDs can contain any type of object including user defined classes.

- A RDD is simply an encapsulation of a very large dataset.
  
- In Spark, all work is expressed as either creating new RDDs, transforming existing RDDs, or calling operations on RDDs to compute a result.

- Under the hood, Spark automatically distributes data contained in RDDs across your cluster and parallelize the operations you perform on them.

- What can we do with RDDs?
  - Transformations
    Apply some functions to the data in RDD to create a new RDD e.g.
    
    ```scala
    val lines = sc.textFile("uppercase.txt")
    val linesWithFriday = lines.filter(_.contains("Friday"))
    ```
  
  - Actions
    Compute a result based on a RDD e.g.
    
    ```scala
    val linesWithFriday = sc.textFile("uppercase.txt").filter(_.contains("Friday"))
    val linesWithFridayResult = lines.first()
    ```
    
## Spark RDD General Workflow

- Generate initial RDDs from external data.

- Apply transformations.

- Launch actions.

## Create a RDD

Take an existing collection and give it to SparkContext's **parallelize** method e.g.

```scala
val inputIntegers = List(1, 2, 3, 4, 5)
val integerRdd = sc.parallelize(inputIntegers)
```

Here, all the elements in the collection will be copied to form a distributed dataset that can be operated on in parallel.
This example is obviously not practical for large datasets, which would attempt to load all data into memory.

Note that **SparkContext** represents a connection to a computing cluster.Now even though using **sc.textFile** is more practical, in reality the external storage will be a distributed file system such as **Amazon S3** or **HDFS**.
And more such as **Cassandra**, **Elasticsearch** etc.

## RDDs are Distributed

- Each RDD is broken into multiple pieces called partitions, and these partitions are divided across the clusters.

## Lazy Evaluation

```scala
// Nothing would happen when Spark sees textFile() statement
val lines = sc textFile "in/uppercase.txt"

// Nothing would happen when Spark sees filter() transformation
val linesWithFriday = lines.filter(_.startsWith("Friday"))

// Spark only starts loading uppercase.txt when first() action is called on linesWithFriday
linesWithFriday.first()

// Spark scans the files only until the first line starting with Friday is detected
// It doesn't even need to go through the entire file
```

- Transformation return another RDD

  ```scala
  val lines = sc textFile "in/uppercase.txt"
  
  def textFile(path: String, minPartitions: Int = defaultMinPartitions): RDD[String]
  ```

- Actions return some other data type

  ```scala
  val first = lines.first()
  
  def first(): T
  ```

  