# Spark

Spark vs Hadoop MapReduce:
- MapReduce stores data on disk
- Spark is a faster processing engine storing intermediate results in memory
- Spark can write to disk when it runs out of memory
- MapReduce can only work with HDFS data
- Spark can access data from diverse data sources: HDFS, local, cloud storage e.g. S3, NoSQL database
- Spark has its own cluster manager (Spark Scheduler) and can leverage 3rd party cluster managers such as YARN and Mesos

**Resilient Distributed Datasets (RDD)** is the fundamental data structure of Spark:
- RDDs are created using **SparkContext**
- SparkContext partitions RDDs and distributes them across the cluster
- This is how Spark does parallel processing of data
- RDDs are stored in distributed manner across the cluster of computers
- If one node goes down, data will still be retained in memory of other nodes
- This is how Spark achieves fault tolerance

**SparkSQL** was introduced to help those who struggled using RDDs directly:
- SparkSQL is built on top of RDD and support DataFrame