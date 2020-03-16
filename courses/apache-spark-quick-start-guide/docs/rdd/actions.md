# Actions

So far in every example we used the **collect()** method to get the output. To get the final result back to the driver, Spark provides another type of operation known as *actions*. At the time of transformations, Spark chains these operations and constructs a DAG, but nothing gets executed. Once an action is performed on an RDD, it forces the evaluation of all the transformations required to compute that RDD.

Actions do not create a new RDD. They are used for the following:

- Returning final results to the driver
- Writing final result to an external storage
- Performing some operation on each element of that RDD (for example, foreach())

The **collect()** action returns all the elements of an RDD to the driver program. You should only use collect() if you are sure about the size of your final output. If the size of the final output is huge, then your driver program might crash while receiving the data from the executors. The use of collect()is not advised in production.

Other **actions** include:

- count
- take
- top
- takeOrdered
- first
- countByValue
- reduce
- saveAsTextFile
- foreach

Examples:

```scala
scala> sc.parallelize(List("A", "A", "B", "B", "C", "A")).countByValue
res0: scala.collection.Map[String,Long] = Map(A -> 3, B -> 2, C -> 1)
```

```scala
scala> sc.parallelize(1 to 10).reduce(_ + _)
res1: Int = 55
```

To save the results to an external data store, we can make use of **saveAsTextFile()** to save your result in a directory. You can also specify a compression codec to store your data in compressed form. We provide a directory as an argument, and Spark writes data inside this directory in multiple files, along with the success file (_success).

```scala
scala> sc.parallelize(1 to 10).saveAsTextFile("testing-save")
```

```bash
spark-backwards/courses/apache-spark-quick-start-guide/target
➜ ls -las testing-save
total 216
0 drwxr-xr-x  36 davidainslie  staff  1152 16 Mar 11:39 .
0 drwxr-xr-x   5 davidainslie  staff   160 16 Mar 11:39 ..
8 -rw-r--r--   1 davidainslie  staff     8 16 Mar 11:39 ._SUCCESS.crc
8 -rw-r--r--   1 davidainslie  staff     8 16 Mar 11:39 .part-00000.crc
8 -rw-r--r--   1 davidainslie  staff    12 16 Mar 11:39 .part-00001.crc
8 -rw-r--r--   1 davidainslie  staff     8 16 Mar 11:39 .part-00002.crc
8 -rw-r--r--   1 davidainslie  staff    12 16 Mar 11:39 .part-00003.crc
8 -rw-r--r--   1 davidainslie  staff    12 16 Mar 11:39 .part-00004.crc
8 -rw-r--r--   1 davidainslie  staff     8 16 Mar 11:39 .part-00005.crc
8 -rw-r--r--   1 davidainslie  staff    12 16 Mar 11:39 .part-00006.crc
8 -rw-r--r--   1 davidainslie  staff    12 16 Mar 11:39 .part-00007.crc
8 -rw-r--r--   1 davidainslie  staff     8 16 Mar 11:39 .part-00008.crc
8 -rw-r--r--   1 davidainslie  staff    12 16 Mar 11:39 .part-00009.crc
8 -rw-r--r--   1 davidainslie  staff     8 16 Mar 11:39 .part-00010.crc
8 -rw-r--r--   1 davidainslie  staff    12 16 Mar 11:39 .part-00011.crc
8 -rw-r--r--   1 davidainslie  staff    12 16 Mar 11:39 .part-00012.crc
8 -rw-r--r--   1 davidainslie  staff     8 16 Mar 11:39 .part-00013.crc
8 -rw-r--r--   1 davidainslie  staff    12 16 Mar 11:39 .part-00014.crc
8 -rw-r--r--   1 davidainslie  staff    12 16 Mar 11:39 .part-00015.crc
0 -rw-r--r--   1 davidainslie  staff     0 16 Mar 11:39 _SUCCESS
0 -rw-r--r--   1 davidainslie  staff     0 16 Mar 11:39 part-00000
8 -rw-r--r--   1 davidainslie  staff     2 16 Mar 11:39 part-00001
0 -rw-r--r--   1 davidainslie  staff     0 16 Mar 11:39 part-00002
8 -rw-r--r--   1 davidainslie  staff     2 16 Mar 11:39 part-00003
8 -rw-r--r--   1 davidainslie  staff     2 16 Mar 11:39 part-00004
0 -rw-r--r--   1 davidainslie  staff     0 16 Mar 11:39 part-00005
8 -rw-r--r--   1 davidainslie  staff     2 16 Mar 11:39 part-00006
8 -rw-r--r--   1 davidainslie  staff     2 16 Mar 11:39 part-00007
0 -rw-r--r--   1 davidainslie  staff     0 16 Mar 11:39 part-00008
8 -rw-r--r--   1 davidainslie  staff     2 16 Mar 11:39 part-00009
0 -rw-r--r--   1 davidainslie  staff     0 16 Mar 11:39 part-00010
8 -rw-r--r--   1 davidainslie  staff     2 16 Mar 11:39 part-00011
8 -rw-r--r--   1 davidainslie  staff     2 16 Mar 11:39 part-00012
0 -rw-r--r--   1 davidainslie  staff     0 16 Mar 11:39 part-00013
8 -rw-r--r--   1 davidainslie  staff     2 16 Mar 11:39 part-00014
8 -rw-r--r--   1 davidainslie  staff     3 16 Mar 11:39 part-00015
```

