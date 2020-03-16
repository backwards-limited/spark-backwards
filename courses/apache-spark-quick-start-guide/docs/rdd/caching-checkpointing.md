# Caching and Checkpointing

You can cache large datasets in-memory or on-disk depending upon your cluster hardware. You can choose to cache your data in two scenarios:

- Use the same RDD multiple times
- Avoid reoccupation of an RDD that involves heavy computation, such as join() and groupByKey()

```scala
scala> val rdd = sc.parallelize(1 to 10)
rdd: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[15] at parallelize at <console>:24

scala> rdd.cache
res7: rdd.type = ParallelCollectionRDD[15] at parallelize at <console>:24

scala> rdd.take(2)
res8: Array[Int] = Array(1, 2)

scala> rdd.count
res9: Long = 10
```

The life cycle of the cached RDD will end when the Spark session ends. If you have computed an RDD and you want it to use in another Spark program without recomputing it, then you can make use of the checkpoint() operation. This allows storing the RDD content on the disk, which can be used for the later operations:

```scala
scala> val rdd = sc.parallelize(List("A", "B", "C"))
rdd: org.apache.spark.rdd.RDD[String] = ParallelCollectionRDD[16]

scala> sc.setCheckpointDir("checkpointing")

scala> rdd.checkpoint
```

We first create a baseRDD and set a checkpointing directory using setCheckpointDir() method. Finally, we store the content of baseRDD using checkpoint().

```bash
spark-backwards/courses/apache-spark-quick-start-guide/target
➜ ls -las checkpointing
total 0
0 drwxr-xr-x  3 davidainslie  staff   96 16 Mar 15:33 .
0 drwxr-xr-x  6 davidainslie  staff  192 16 Mar 15:33 ..
0 drwxr-xr-x  2 davidainslie  staff   64 16 Mar 15:33 0aee6441-bcaa-4044-832a-6f06ee37f42a
```

