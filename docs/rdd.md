# Resilient Distributed Datasets (RDD)

The RDD is the fundamental abstraction in Spark. It represents a collection of elements that is:

- *Immutable* (read-only) 
- *Resilient* (fault-tolerant) 
- *Distributed* (dataset spread out to more than one node) 

There are two types of RDD operations: transformations and actions.

- **Transformations** (e.g. filter, map) are operations that produce a new RDD by performing
  some useful data manipulation on another RDD.
- **Actions** (e.g. count, foreach, collect) trigger a computation in order to return the result to the calling program or
  to perform some actions on an RDD’s elements.

- So what is a dataset?

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

## Example

We have GitHub archive files under the [github-archive](../src/main/resources/github-archive) directory.

Take a look at one line of one of the files:

```bash
$ head -n 1 2015-03-01-0.json | jq '.'

{
  "id": "2614896652",
  "type": "CreateEvent",
  "actor": {
    "id": 739622,
    "login": "treydock",
    "gravatar_id": "",
    "url": "https://api.github.com/users/treydock",
    "avatar_url": "https://avatars.githubusercontent.com/u/739622?"
  },
  "repo": {
    "id": 23934080,
    "name": "Early-Modern-OCR/emop-dashboard",
    "url": "https://api.github.com/repos/Early-Modern-OCR/emop-dashboard"
  },
  "payload": {
    "ref": "development",
    "ref_type": "branch",
    "master_branch": "master",
    "description": "",
    "pusher_type": "user"
  },
  "public": true,
  "created_at": "2015-03-01T00:00:00Z",
  "org": {
    "id": 10965476,
    "login": "Early-Modern-OCR",
    "gravatar_id": "",
    "url": "https://api.github.com/orgs/Early-Modern-OCR",
    "avatar_url": "https://avatars.githubusercontent.com/u/10965476?"
  }
}
```

Note, we are going to use **DataFrame** (there are **DataSet** which are generalized and improved type of DataFrame):

A DataFrame is an RDD that has a schema. You can think of it as a relational database table, in that each column has a name and a known type. The power of DataFrames comes from the fact that, when you create a DataFrame from a structured dataset (in this case, JSON), Spark is able to infer a schema by making a pass over the entire JSON dataset that’s being loaded. Then, when calculating the execution plan, Spark can use the schema and do substantially better computation optimizations.  