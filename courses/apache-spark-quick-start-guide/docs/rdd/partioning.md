# Partioning

Data partitioning plays a really important role in distributed computing, as it defines the degree of parallelism for the applications. Understating and defining partitions in the right way can significantly improve the performance of Spark jobs. There are two ways to control the degree of parallelism for RDD operations:

- repartition() and coalesce()
- partitionBy()

Partitions of an existing RDD can be changed using repartition() or coalesce(). These operations can redistribute the RDD based on the number of partitions provided. The repartition() can be used to increase or decrease the number of partitions, but it involves heavy data shuffling across the cluster. On the other hand, coalesce() can be used only to decrease the number of partitions. In most of the cases, coalesce() does not trigger a shuffle.

Any operation that shuffles the data accepts an additional parameter, that is, degrees of parallelism:

```scala
scala> val rdd = sc.parallelize(List("US" -> 20, "IND" -> 30, "UK" -> 10), 3)
rdd: org.apache.spark.rdd.RDD[(String, Int)] = ParallelCollectionRDD[17]

scala> rdd.getNumPartitions
res12: Int = 3
```

```scala
scala> val groupedRdd = rdd.groupByKey(2)
groupedRdd: org.apache.spark.rdd.RDD[(String, Iterable[Int])] = ShuffledRDD[18]

scala> groupedRdd.getNumPartitions
res13: Int = 2
```

